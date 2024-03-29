package cat.dam.ivan.trucadainsistent;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import static android.app.PendingIntent.FLAG_IMMUTABLE;

import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    //members
    private static final int MAX_PHONE_LENGTH=18;
    private static final String PHONE_NUMBER_PATTERN = "^(\\+[1-9][0-9]{1,14}|[0-9]{7,15})$";
    //    The regular expression ^(\\+[1-9][0-9]{1,14}|[0-9]{7,15})$ matches either a phone number
    //    with an optional country code, or a phone number without a country code.
    //    The country code must start with a plus sign + followed by a digit between 1 and 9,
    //    and the mobile number must have between 7 and 15 digits.
    //    The vertical bar | is used to separate the two alternative patterns.
    //  The regular expression ^[\\p{L}0-9\\s\\p{Punct}]+$ matches a sequence of characters
    //  that may include letters, digits, whitespaces, and punctuation marks.
    //  The \\p{L} character class matches any letter from any alphabet,
    //  the 0-9 character class matches any digit,
    //  the \\s character class matches any whitespace character,
    //  and the \\p{Punct} character class matches any punctuation mark.
    //  The ^ and $ anchors are used to ensure that the entire string matches the pattern.
    //  The {} quantifier is used to match one to MAX characters.

    private static final int MAX_INTENTS = 5;
    private static final int[] btnNumbers = new int[]{R.id.btn_num_0, R.id.btn_num_1, R.id.btn_num_2, R.id.btn_num_3, R.id.btn_num_4,
            R.id.btn_num_5, R.id.btn_num_6, R.id.btn_num_7, R.id.btn_num_8, R.id.btn_num_9};
    private static PermissionManager permissionCallManager, permissionSmsManager;
    private static final ArrayList<PermissionData> permissionsCallRequired=new ArrayList<>();
    private ProgressBar progressBar;
    private TextView tv_phone;
    private Button btn_call, btn_delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initPermissions();
        initListeners();
    }

    private void initViews() {
        //et_sms = findViewById(R.id.et_sms);
        progressBar = findViewById(R.id.pb_progressBar);
        tv_phone = findViewById(R.id.tv_phone);
        btn_call = findViewById(R.id.btn_send_call);
        btn_delete = findViewById(R.id.btn_delete_digit);
    }

    private void initPermissions() {
        //TO DO: CONFIGURE ALL NECESSARY PERMISSIONS
        //BEGIN
        permissionsCallRequired.add(new PermissionData(Manifest.permission.CALL_PHONE,
                getString(R.string.callPermissionNeeded),
                "",
                getString(R.string.callPermissionThanks),
                getString(R.string.callPermissionSettings)));

        //END
        //DON'T DELETE == call permission manager ==
        permissionCallManager= new PermissionManager(this, permissionsCallRequired);

    }

    private void initListeners() {

        this.btn_call.setOnClickListener(v -> {
            if (!permissionCallManager.hasAllNeededPermissions(this, permissionsCallRequired))
            { //Si manquen permisos els demanem
                permissionCallManager.askForPermissions(this, permissionCallManager.getRejectedPermissions(this, permissionsCallRequired));
            } else {
                //Si ja tenim tots els permisos, truquem
                callPhone();
            }
        });

        this.btn_delete.setOnClickListener(v -> {
            if (tv_phone.length() != 0) {
                tv_phone.setText(tv_phone.getText().toString().substring(0, tv_phone.length() - 1));
            }
        });
    }

    void callPhone() {
        String phone_number_pattern = tv_phone.getText().toString();
        if (isValidPhoneNumber(phone_number_pattern)) {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone_number_pattern));
            Thread thread = new Thread(() -> {
                while (progressBar.getProgress() < MAX_INTENTS)
                {

                    progressBar.setProgress(progressBar.getProgress() + 1);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    startActivity(intent);
                }

            });
            thread.start();
        } else createToast(getResources().getString(R.string.phone_wrong_format));
    }

    private Boolean isValidPhoneNumber(String phone_number) {
        //VALIDATE PHONE NUMBER WITH REGEX
        Pattern pattern = Pattern.compile(PHONE_NUMBER_PATTERN);
        return pattern.matcher(phone_number).matches();
    }

    public void onClickNumbers(View view) {
        int digit;
        if (tv_phone.length() < MAX_PHONE_LENGTH) {
            //using text labels instead of numbers
            digit = indexOf(view.getId());
            // add digit to phone number
            StringBuilder phone = new StringBuilder(tv_phone.getText().toString());
            phone.append(digit);
            this.tv_phone.setText(phone.toString());
        }
    }

    private int indexOf(int search) {
        int i = 0;
        boolean found = false;
        while (i < MainActivity.btnNumbers.length && !found) {
            if (MainActivity.btnNumbers[i] == search) found = true;
            else i++;
        }
        if (i == MainActivity.btnNumbers.length) return -1;
        else return i;
    }

    private void createToast(String text) {
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
    }

}