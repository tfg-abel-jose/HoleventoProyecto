package es.ideas.holeventoproyecto;

import static es.ideas.holeventoproyecto.utils.utils.obtenerNombreUsuario;
import static es.ideas.holeventoproyecto.utils.utils.obtenerUid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import es.ideas.holeventoproyecto.auth.LoginActivity;
import es.ideas.holeventoproyecto.auth.RegisterUserActivity;
import es.ideas.holeventoproyecto.fragments.business.NuevoEvento;
import es.ideas.holeventoproyecto.fragments.business.Profile;

public class BusinessMainActivity extends AppCompatActivity {

    private BottomNavigationView bv;
    private Fragment currentFragment;
    private TextView username;
    private ImageButton logout;

    private static final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.business_main_activity);

        username = (TextView) findViewById(R.id.nombreUserBusiness);
        logout = (ImageButton) findViewById(R.id.btnLogout);

        username.setText(obtenerNombreUsuario());
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(BusinessMainActivity.this, LoginActivity.class));
                finish();
            }
        });

        //Si se rota el móvil, no nos cargará un nuevo fragment, por lo que
        //no perderemos los datos que tenemos en pantalla.
        if (savedInstanceState == null) {
            currentFragment = new Profile();
            cambiaFragment(currentFragment);
        } else {
            cambiaFragment(currentFragment);
        }


        //Se crea la BottomNavigationBar, y dependiendo de donde hagamos clic, carga el fragment
        //del mapa, o el fragment con el historial de recorridos.
        bv = (BottomNavigationView) findViewById(R.id.navBar);
        bv.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.profile:
                        currentFragment = new Profile();
                        break;

                    case R.id.newEvent:
                        currentFragment = new NuevoEvento();
                        break;
                }
                cambiaFragment(currentFragment);
                return true;
            }
        });


    }

    /**
     * Se le pasa un fragmento, el cuál declararemos en el NavBar, y dependiendo del resultado
     * cargará un fragment u otro.
     *
     * @param f Fragmento que recibe, pora crear uno del mismo.
     */
    private void cambiaFragment(Fragment f) {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_Layout, f).commit();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}