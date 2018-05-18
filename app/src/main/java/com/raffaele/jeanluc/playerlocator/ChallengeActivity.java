package com.raffaele.jeanluc.playerlocator;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ChallengeActivity extends AppCompatActivity {

    final String ACCEPTED = "accepted";
    final String PENDING = "pending";
    final String REJECTED = "rejected";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);
        String username = getSharedPreferences("PlayerLocator", MODE_PRIVATE).getString("username", "UNKNOWN");
        List<ChallengeInfo> challenges = LoadChallenges(username);

        LinearLayout myRoot = (LinearLayout)findViewById(R.id.challenge_root_layout);
        LinearLayout a = new LinearLayout(this);
        a.setOrientation(LinearLayout.VERTICAL);


        for (ChallengeInfo c : challenges)
        {
            TextView newview = new TextView(this);
            newview.setClickable(true);
            newview.setTextSize(20);
            newview.setPadding(0, 15, 0, 15);
            String viewtext;


            if (c.challenger.equals(username))
            {
                viewtext = "Your challenge vs " + c.challengee + " is: " + c.challenge_status;
                newview.setText(viewtext);

                newview.setOnClickListener(new ChallengeClickListener(true, c));

            }
            else
            {
                viewtext = "You have been challenged by " + c.challenger + "!";
                newview.setText(viewtext);

                newview.setOnClickListener(new ChallengeClickListener(false, c));


            }



            if (c.challenge_status.equals("pending"))
                newview.setBackgroundResource(R.color.PendingBackground);
            else if (c.challenge_status.equals("accepted"))
                newview.setBackgroundResource(R.color.AcceptedBackground);
            else
                newview.setBackgroundResource(R.color.RejectedBackground);



            a.addView(newview);
        }

        myRoot.addView(a);


    }


    public class ChallengeClickListener implements View.OnClickListener
    {

        //String title;
        //String message;
        //String status;

        ChallengeInfo challenge;
        Boolean is_challenger;

        public ChallengeClickListener(Boolean is_challenger, ChallengeInfo challenge) {
            this.is_challenger = is_challenger;
            this.challenge = challenge;
        }





        private AlertDialog createDialog(String title, String message)
        {
                AlertDialog alertDialog = new AlertDialog.Builder(ChallengeActivity.this).setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        //Toast.makeText(ChallengeActivity.this, "Yaay", Toast.LENGTH_SHORT).show();
                    }}) .create();
                alertDialog.setTitle(title);
                alertDialog.setMessage(message);
                alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
                return alertDialog;


        }


        @Override
        public void onClick(View v)
        {
            String title;
            String message;
            Boolean pending_reply = false;
            AlertDialog alertDialog;

            Log.d("challenge_debug", "in on click");
            if (is_challenger)
            {
                switch (challenge.challenge_status)
                {
                    case ACCEPTED:
                        title = "Here is " + challenge.challengee + "'s contact info!";
                        message = getContactInfo(challenge.challengee);
                        break;
                    case REJECTED:
                        title = "Unavailable";
                        message = challenge.challengee + "rejected your challenge :(";
                        break;
                    case PENDING:
                        title = "Pending";
                        message = "Waiting on " + challenge.challengee + " to reply";
                        break;
                    default:
                        title = "Error";
                        message = "Error";
                        break;
                }
                alertDialog = createDialog(title, message);
                alertDialog.show();
            }

            else
            {
                switch (challenge.challenge_status)
                {
                    case ACCEPTED:
                        title = "Here is " + challenge.challenger + "'s contact info!";
                        message = getContactInfo(challenge.challenger);
                        break;
                    case REJECTED:
                        title = "Not available";
                        message = "You rejected " + challenge.challenger + "'s challenge";
                        break;
                    case PENDING:
                        title = "New Challenge!";
                        message = challenge.challenger + " has challenged you! (This will share your contact information from settings)";
                        pending_reply = true;
                        break;
                    default:
                        title = "Error";
                        message = "Error";
                        break;
                }


                alertDialog = createDialog(title, message);
                if (pending_reply) {
                    alertDialog.setButton(Dialog.BUTTON_POSITIVE,"ACCEPT", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            sendResponse(ACCEPTED, challenge.challenger, challenge.challengee);
                        }
                    });
                    alertDialog.setButton(Dialog.BUTTON_NEGATIVE,"DECLINE", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            sendResponse(REJECTED, challenge.challenger, challenge.challengee);
                        }
                    });
                    alertDialog.setButton(Dialog.BUTTON_NEUTRAL,"CANCEL", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(ChallengeActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                        }
                    });
                }


                alertDialog.show();
            }
        }


        private String sendResponse(String response, String challenger, String challengee)
        {
            String z = "";
            ConnectionClass connectionClass;
            connectionClass = new ConnectionClass();


            try {
                Connection conn = connectionClass.CONN();

                if (conn != null) {



                    String query = "UPDATE Challenges SET challenge_status = '" + response + "' " +
                            "WHERE challenger = '" + challenger + "' AND challengee = '" + challengee + "'";
                    Statement stmt = conn.createStatement();
                    stmt.executeUpdate(query);

                    z = "success";
                }
            }
            catch(Exception ex)
            {
                z = "Error getting contact info";
            }
            return z;
        }

        private String getContactInfo(String lookup)
        {
            String z = "";
            ConnectionClass connectionClass;
            connectionClass = new ConnectionClass();


            try {
                Connection conn = connectionClass.CONN();

                if (conn != null) {


                    //Check to see if challenge exists
                    String query = "SELECT contactInfo FROM UserInfo WHERE id = (SELECT id FROM Users WHERE username = '" + lookup + "')";
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    rs.next();
                    z = rs.getString("contactInfo");

                }
            }
            catch(Exception ex)
            {
                z = "Error getting contact info";
            }
            return z;
        }
    }






    class ChallengeInfo
    {
        String challenger;
        String challengee;
        String challenge_status;

        public ChallengeInfo(String challenger, String challengee, String challenge_status)
        {
            this.challenger = challenger;
            this.challengee = challengee;
            this.challenge_status = challenge_status;
        }
    }


    public List<ChallengeInfo> LoadChallenges(String name)
    {
        ConnectionClass connectionClass;
        connectionClass = new ConnectionClass();
        List<ChallengeInfo> challenges = new ArrayList<>();
        String challenger;
        String challengee;
        String challenge_status;

        try {
            Connection conn = connectionClass.CONN();

            if (conn != null) {


                //Check to see if challenge exists
                String query = "SELECT * FROM Challenges WHERE challenger = "
                        + "'" + name + "'" + " OR challengee = " + "'" + name + "' AND challenge_date Between DATEADD(m, -1, GETDATE()) and GETDATE()";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                while(rs.next())
                {
                    challenger = rs.getString("challenger");
                    challengee = rs.getString("challengee");
                    challenge_status = rs.getString("challenge_status");

                    challenges.add(new ChallengeInfo(challenger, challengee, challenge_status));
                }


            }
        }
        catch(Exception ex)
        {
            Log.d("challenge_debug", ex.getMessage());
        }
        return challenges;
    }

}


