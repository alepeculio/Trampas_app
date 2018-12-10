package com.trampas.trampas;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.trampas.trampas.Adaptadores.MostrarTrampasColocadasInterface;
import com.trampas.trampas.Clases.Usuario;

import java.util.Objects;

public class MenuPrincipal extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MostrarTrampasColocadasInterface {
    Usuario usuario = null;
    String idColocacionCreada;
    TextView nombre;
    TextView correo;
    int opcionSeleccionada = 0;
    ColocarTrampa colocarTrampa;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usuario = getUsuario();

        if (usuario != null) {
            setContentView(R.layout.activity_menu_principal);
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);


            View headerView = navigationView.getHeaderView(0);
            nombre = headerView.findViewById(R.id.tvNombre);
            correo = headerView.findViewById(R.id.tvCorreo);
            nombre.setText(usuario.getNombre() + " " + usuario.getApellido());
            correo.setText(usuario.getCorreo());

            cargarOpcionSeleccionada();
        }
    }

    //Obtener el usuario loguado o ir al iniciar sesión si no lo está.
    public Usuario getUsuario() {
        SharedPreferences sp = getSharedPreferences("usuario_guardado", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sp.getString("usuario", null);

        if (json != null)
            return gson.fromJson(json, Usuario.class);
        else {
            cerrarSesion();
            return null;
        }

    }

    //Borrar al usuario logueado guardado y dirigirse al iniciar sesión.
    public void cerrarSesion() {
        SharedPreferences sp = getSharedPreferences("usuario_guardado", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.clear();
        ed.apply();

        borrarOpcionSeleccionada();

        Intent i = new Intent(MenuPrincipal.this, Login.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
    }

    //Cargar la opción seleccionada del menú guardada si existe.
    private void cargarOpcionSeleccionada() {
        NavigationView navigationView = findViewById(R.id.nav_view);

        if (usuario.getAdmin() == 3) {
            navigationView.getMenu().findItem(R.id.nav_mostrar_exitentes).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_colocar).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_extraer).setVisible(false);
        }

        if (usuario.getAdmin() != 1) {
            navigationView.getMenu().findItem(R.id.nav_administrar_usuarios).setVisible(false);
        }

        try {
            opcionSeleccionada = obtenerOpcionSeleccionada();
            if (opcionSeleccionada != 0) {
                MenuItem mi = navigationView.getMenu().findItem(opcionSeleccionada);
                if (mi != null) {
                    onNavigationItemSelected(mi);
                    navigationView.setCheckedItem(opcionSeleccionada);
                } else
                    throw new Exception();

            } else
                throw new Exception();
        } catch (Exception e) {
            if (usuario.getAdmin() != 3) {
                onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_mostrar_exitentes));
                navigationView.setCheckedItem(R.id.nav_mostrar_exitentes);
            } else {
                onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_mostrar_colocadas));
                navigationView.setCheckedItem(R.id.nav_mostrar_colocadas);
            }
        }
    }

    //Guardar la opción del menú seleccionada.
    private void guardarOpcionSeleccionada() {
        SharedPreferences sp = getSharedPreferences(getString(R.string.opcion_menu), MODE_PRIVATE);
        SharedPreferences.Editor et = sp.edit();
        et.putString("opcion", String.valueOf(opcionSeleccionada));
        et.apply();
    }

    //Recuperar opción del menú guardada.
    private int obtenerOpcionSeleccionada() {
        SharedPreferences sp = getSharedPreferences(getString(R.string.opcion_menu), MODE_PRIVATE);
        return Integer.valueOf(sp.getString("opcion", null));
    }

    //Borrar opción del menú guardada.
    private void borrarOpcionSeleccionada() {
        SharedPreferences sp = getSharedPreferences(getString(R.string.opcion_menu), MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.clear();
        ed.apply();
        opcionSeleccionada = 0;
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        cargarOpcionSeleccionada();
    }*/

    //Al detenerse la actividad, guardar la opción del menú actual.
    @Override
    protected void onStop() {
        super.onStop();
        guardarOpcionSeleccionada();
    }

    //Abrir menú al presionar la tecla de atrás.
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (drawer.isDrawerOpen(Gravity.START)) {
                this.finishAffinity();
            } else {
                Toast.makeText(this, R.string.mensaje_salir, Toast.LENGTH_SHORT).show();
                drawer.openDrawer(Gravity.START);
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    //Al seleccionar un item en el menú
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        opcionSeleccionada = id;
        Fragment fragment = null;
        boolean fragmentTransaction = false;

        switch (id) {
            case R.id.nav_colocar:
                fragment = new ColocarTrampa();
                colocarTrampa = (ColocarTrampa) fragment;
                colocarTrampa.setUsuario(usuario);
                fragmentTransaction = true;
                Objects.requireNonNull(getSupportActionBar()).setTitle("Colocar trampa");
                break;
            case R.id.nav_extraer:
                fragment = new ExtraerTrampa();
                fragmentTransaction = true;
                Objects.requireNonNull(getSupportActionBar()).setTitle("Extraer trampa");
                break;
            case R.id.nav_mostrar_colocadas:
                fragment = new MostrarTrampasColocadas();
                fragmentTransaction = true;
                ((MostrarTrampasColocadas) fragment).setUsuario(usuario);
                Objects.requireNonNull(getSupportActionBar()).setTitle("Trampas colocadas");
                if (idColocacionCreada != null) {
                    ((MostrarTrampasColocadas) fragment).setIdColocacionCreada(idColocacionCreada);
                    idColocacionCreada = null;
                }
                break;
            case R.id.nav_mostrar_exitentes:
                fragment = new MostrarTrampasExistentes();
                fragmentTransaction = true;
                Objects.requireNonNull(getSupportActionBar()).setTitle("Trampas existentes");
                ((MostrarTrampasExistentes) fragment).setUsuario(usuario);
                break;
            case R.id.nav_perfil:
                fragment = new Perfil();
                fragmentTransaction = true;
                Objects.requireNonNull(getSupportActionBar()).setTitle("Mi perfil");
                ((Perfil) fragment).setUsuario(usuario);
                break;
            case R.id.nav_administrar_usuarios:
                fragment = new AdministrarUsuarios();
                fragmentTransaction = true;
                Objects.requireNonNull(getSupportActionBar()).setTitle("Administración usuarios");
                ((AdministrarUsuarios) fragment).setUsuario(usuario);
                break;
            case R.id.nav_desarrollado:
                fragment = new Desarrollado();
                fragmentTransaction = true;
                Objects.requireNonNull(getSupportActionBar()).setTitle("Desarrollado por");
                break;
            case R.id.nav_cerrar_sesion:
                cerrarSesion();
                break;
        }

        if (fragmentTransaction) {
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.contenido_seleccionado, fragment).commit();
            item.setChecked(true);
            //Drawable icon = item.getIcon();
            //icon.mutate().setColorFilter(getResources().getColor(R.color.colorBlanco), PorterDuff.Mode.SRC_ATOP);
            //getSupportActionBar().setIcon(icon);

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void irAlMapa(String idColocacion) {
        idColocacionCreada = idColocacion;
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_mostrar_colocadas);
        onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_mostrar_colocadas));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (colocarTrampa != null && requestCode == ColocarTrampa.SOLICITUD_LOCALIZACION)
            colocarTrampa.onActivityResult(requestCode, resultCode, data);
        else
            super.onActivityResult(requestCode, resultCode, data);

    }

}
