package com.a7f.drawingsound;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.a7f.drawingsound.lib.NetworkCheck;
import com.a7f.drawingsound.lib.PermissionCheck;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SigninActivity extends AppCompatActivity {

    private Button ButtonLogin;
    private Button ButtonSignup;
    private EditText EditTextEmail;
    private EditText EditTextPasswd;

    private FirebaseAuth mAuth;
    private NetworkCheck networkcheck;

    private long backKeyPressedTime = 0;
    private Toast toast;

    ProgressDialog asyncDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        PermissionCheck permissioncheck = new PermissionCheck();

        permissioncheck.PRead(SigninActivity.this);
        permissioncheck.PRecord(SigninActivity.this);
        permissioncheck.PWrite(SigninActivity.this);

        setHandler();
    }

    private void setHandler(){
        ButtonLogin = (Button)findViewById(R.id.ButtonLogin);
        ButtonSignup = (Button)findViewById(R.id.ButtonSignup);

        EditTextEmail = (EditText)findViewById(R.id.EditTextEmail);
        EditTextPasswd = (EditText)findViewById(R.id.EditTextPasswd);
        EditTextPasswd.setTransformationMethod(new PasswordTransformationMethod());

        ButtonLogin.setOnClickListener(LoginClickListener);
        ButtonSignup.setOnClickListener(SignupClickListener);

        mAuth = FirebaseAuth.getInstance();

        networkcheck = new NetworkCheck(SigninActivity.this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent=new Intent(SigninActivity.this,SetActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void signIn(String email, String password) {
        final String TAG = "LoginWithEmail";
        Log.d(TAG, "signIn:" + email);

        // 로딩 중 progressdialog 생성
        asyncDialog = new ProgressDialog(SigninActivity.this);
        asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        asyncDialog.setMessage("로그인 중입니다");
        asyncDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 로그인 성공 시, progrerssdialog를 끄기
                            asyncDialog.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            Toast.makeText(SigninActivity.this, "Authentication success.",
                                    Toast.LENGTH_SHORT).show();

                            // setactivity로 갈 때, 태스크를 새로 생성하여 클릭이 중복되어도 activity 하나만 생성
                            Intent intent=new Intent(SigninActivity.this,SetActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            // 로그인 실패 시 progressdialog를 끄기
                            asyncDialog.dismiss();
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(SigninActivity.this, "Authentication failed. 이메일과 비번확인",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
        });
    }


    Button.OnClickListener LoginClickListener = new View.OnClickListener() {
        public void onClick(View v){
            String email,passwd;
            email = EditTextEmail.getText().toString();
            passwd = EditTextPasswd.getText().toString();

            if(!email.isEmpty() && !passwd.isEmpty()){
                if(networkcheck.checkNetwork()) {
                    signIn(email, passwd);
                }
            }else{
                Toast.makeText(SigninActivity.this, "이메일과 비밀번호 입력하세요",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    Button.OnClickListener SignupClickListener = new View.OnClickListener() {
        public void onClick(View v){
            Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
            if(networkcheck.checkNetwork()) {
                startActivity(intent);
            }
        }
    };

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(getApplicationContext(), "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            //moveTaskToBack(true);
            finish();
            toast.cancel();
        }
    }

}
