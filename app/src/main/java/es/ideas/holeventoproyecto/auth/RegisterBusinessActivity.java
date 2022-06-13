package es.ideas.holeventoproyecto.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import es.ideas.holeventoproyecto.R;
import es.ideas.holeventoproyecto.modelo.Provincia;
import es.ideas.holeventoproyecto.modelo.UsuarioBusiness;
import es.ideas.holeventoproyecto.utils.Utils;

public class RegisterBusinessActivity extends AppCompatActivity {

    private EditText registerEmail, registerUsuario, registerTelefono, registerPass, registerPassR;
    private Spinner registerProvincia;
    private Button btnRegistro;
    private Utils util;

    private FirebaseAuth auth;
    private FirebaseFirestore database;
    private AwesomeValidation awesomeValidation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.register_business_user_act);

        // Cargar librerías necesarias.
        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);

        iniciarVista();

        btnRegistro.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String email = registerEmail.getText().toString();
                String pass = registerPass.getText().toString();

                if (awesomeValidation.validate()) {
                    registrar(email, pass);
                }
            }
        });
    }

    private void iniciarVista() {
        util = new Utils();
        registerEmail = findViewById(R.id.registerEmail);
        registerUsuario = findViewById(R.id.registerUsuario);
        registerTelefono = findViewById(R.id.registerTelefono);
        registerPass = findViewById(R.id.registerPass);
        registerPassR = findViewById(R.id.registerPassR);
        btnRegistro = findViewById(R.id.btnRegistro);
        registerProvincia = findViewById(R.id.provincia);
        cargarProvincias();
        comprobarCampos();
    }

    private void comprobarCampos() {
        // Comprueba que los campos no están vacíos.
        awesomeValidation.addValidation(RegisterBusinessActivity.this, R.id.registerEmail,
                RegexTemplate.NOT_EMPTY, R.string.empty);
        awesomeValidation.addValidation(RegisterBusinessActivity.this, R.id.registerUsuario,
                RegexTemplate.NOT_EMPTY, R.string.empty);
        awesomeValidation.addValidation(RegisterBusinessActivity.this, R.id.registerTelefono,
                RegexTemplate.NOT_EMPTY, R.string.empty);
        awesomeValidation.addValidation(RegisterBusinessActivity.this, R.id.registerPass,
                RegexTemplate.NOT_EMPTY, R.string.empty);
        awesomeValidation.addValidation(RegisterBusinessActivity.this, R.id.registerPassR,
                RegexTemplate.NOT_EMPTY, R.string.empty);


        awesomeValidation.addValidation(RegisterBusinessActivity.this, R.id.registerEmail,
                Patterns.EMAIL_ADDRESS, R.string.invalid_mail);
        awesomeValidation.addValidation(RegisterBusinessActivity.this, R.id.registerPass, ".{6,}",
                R.string.invalid_password);
        awesomeValidation.addValidation(RegisterBusinessActivity.this, R.id.registerPassR,
                R.id.registerUserPass, R.string.contrasenya_no_coincide);
        awesomeValidation.addValidation(RegisterBusinessActivity.this, R.id.registerTelefono,
                RegexTemplate.TELEPHONE, R.string.err_tel);
    }

    private void registrar(String email, String pass) {
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            insertaUsuario();
                            Toast.makeText(RegisterBusinessActivity.this, "Usuario registrado.",
                                    Toast.LENGTH_LONG).show();
                            startActivity(new Intent(RegisterBusinessActivity.this,
                                    LoginActivity.class));
                            finish();
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthUserCollisionException e) {
                                Log.d("FALLO", "onComplete: exist_email");
                                Toast.makeText(
                                        RegisterBusinessActivity.this,
                                        "Ya existe una cuenta con este correo electrónico",
                                        Toast.LENGTH_SHORT
                                ).show();
                            } catch (Exception e) {
                                Log.d("FALLO", "onComplete: " + e.getMessage());
                                Toast.makeText(
                                        RegisterBusinessActivity.this,
                                        "Ha ocurrido un error inesperado",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                    }
                });
    }

    private void insertaUsuario() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(registerUsuario.getText().toString())
                .build();
        user.updateProfile(profileUpdates);

        String email = registerEmail.getText().toString();
        String idUsuario = uid;
        String nombreUsuario = registerUsuario.getText().toString();
        String password = registerPass.getText().toString();
        String provincia = (registerProvincia.getSelectedItemPosition()) + "";
        String telefono = registerTelefono.getText().toString();

        UsuarioBusiness usuario = new UsuarioBusiness(email, idUsuario, nombreUsuario, password,
                provincia, telefono);

        database.collection("UsuarioBusiness").document(usuario.getIdUsuario()).set(usuario);
    }

    private void cargarProvincias() {
        List<Provincia> provincias = new ArrayList<>();


        database.collection("Provincias").document("provincia").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                List<String> provincias = (List<String>) document.get("nombre");

                ArrayAdapter<String> arrayAdapter =
                        new ArrayAdapter<>(RegisterBusinessActivity.this,
                                android.R.layout.simple_dropdown_item_1line, provincias);
                registerProvincia.setAdapter(arrayAdapter);
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}