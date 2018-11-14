package com.trampas.trampas;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

public class MenuPrincipal extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MostrarTrampasColocadasInterface {
    Usuario usuario = null;
    String idColocacionCreada;
    TextView nombre;
    TextView correo;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (verificarLogin()) {
            setContentView(R.layout.activity_menu_principal);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);


            View headerView = navigationView.getHeaderView(0);
            nombre = headerView.findViewById(R.id.tvNombre);
            correo = headerView.findViewById(R.id.tvCorreo);
            nombre.setText(usuario.getNombre() + " " + usuario.getApellido());
            correo.setText(usuario.getCorreo());

            cargarOpcionSeleccionada();
        }
    }

    private Boolean verificarLogin() {
        SharedPreferences sp = getSharedPreferences("usuario_guardado", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sp.getString("usuario", null);

        if (json != null) {
            usuario = gson.fromJson(json, Usuario.class);
            return true;
        } else {
            cerrarSesion();
            return false;
        }
    }

    public void cerrarSesion() {
        SharedPreferences sp = getSharedPreferences("usuario_guardado", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.clear();
        ed.commit();

        borrarOpcionSeleccionada();

        Intent i = new Intent(MenuPrincipal.this, Login.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
    }

    private void cargarOpcionSeleccionada() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        if (usuario.getAdmin() == 3) {
            navigationView.getMenu().findItem(R.id.nav_mostrar_exitentes).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_colocar).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_extraer).setVisible(false);
        }

        if(usuario.getAdmin() != 1){
            navigationView.getMenu().findItem(R.id.nav_administrar_usuarios).setVisible(false);
        }

        try {
            int opcion = obtenerOpcionSeleccionada();
            if (opcion != 0) {
                onNavigationItemSelected(navigationView.getMenu().findItem(opcion));
                navigationView.setCheckedItem(opcion);
            }
        } catch (NumberFormatException nfe) {
            if (usuario.getAdmin() != 3) {
                onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_mostrar_exitentes));
                navigationView.setCheckedItem(R.id.nav_mostrar_exitentes);
            } else {
                onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_mostrar_colocadas));
                navigationView.setCheckedItem(R.id.nav_mostrar_colocadas);
            }
        }
    }

    private void guardarOpcionSeleccionada(int opcion) {
        SharedPreferences sp = getSharedPreferences("opcion_seleccionada", MODE_PRIVATE);
        SharedPreferences.Editor et = sp.edit();
        et.putString("opcion", String.valueOf(opcion));
        et.apply();
    }

    private int obtenerOpcionSeleccionada() {
        SharedPreferences sp = getSharedPreferences("opcion_seleccionada", MODE_PRIVATE);
        return Integer.valueOf(sp.getString("opcion", null));
    }

    private void borrarOpcionSeleccionada() {
        SharedPreferences sp = getSharedPreferences("opcion_seleccionada", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.clear();
        ed.commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (drawer.isDrawerOpen(Gravity.LEFT)) {
                this.finishAffinity();
            } else {
                Toast.makeText(this, "Precione nuevamente para salir", Toast.LENGTH_SHORT).show();
                drawer.openDrawer(Gravity.LEFT);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        guardarOpcionSeleccionada(id);
        Fragment fragment = null;
        boolean fragmentTransaction = false;

        switch (id) {
            case R.id.nav_colocar:
                fragment = new ColocarTrampa();
                ((ColocarTrampa) fragment).setUsuario(usuario);
                fragmentTransaction = true;
                getSupportActionBar().setTitle("Colocar trampa");
                break;
            case R.id.nav_extraer:
                fragment = new ExtraerTrampa();
                fragmentTransaction = true;
                getSupportActionBar().setTitle("Extraer trampa");
                break;
            case R.id.nav_mostrar_colocadas:
                fragment = new MostrarTrampasColocadas();
                fragmentTransaction = true;
                ((MostrarTrampasColocadas) fragment).setUsuario(usuario);
                getSupportActionBar().setTitle("Trampas colocadas");
                if (idColocacionCreada != null) {
                    ((MostrarTrampasColocadas) fragment).setIdColocacionCreada(idColocacionCreada);
                    idColocacionCreada = null;
                }
                break;
            case R.id.nav_mostrar_exitentes:
                fragment = new MostrarTrampasExistentes();
                fragmentTransaction = true;
                getSupportActionBar().setTitle("Trampas existentes");
                ((MostrarTrampasExistentes) fragment).setUsuario(usuario);
                break;
            case R.id.nav_perfil:
                fragment = new Perfil();
                fragmentTransaction = true;
                getSupportActionBar().setTitle("Mi perfil");
                ((Perfil) fragment).setUsuario(usuario);
                break;
            case R.id.nav_administrar_usuarios:
                fragment = new AdministrarUsuarios();
                fragmentTransaction = true;
                getSupportActionBar().setTitle("Administración usuarios");
                ((AdministrarUsuarios) fragment).setUsuario(usuario);
                break;
            case R.id.nav_cerrar_sesion:
                cerrarSesion();
                break;
        }

        if (fragmentTransaction) {
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.contenido_seleccionado, fragment, (fragment instanceof ExtraerTrampa) ? "remover" : "").commit();
            item.setChecked(true);
            //Drawable icon = item.getIcon();
            //icon.mutate().setColorFilter(getResources().getColor(R.color.colorBlanco), PorterDuff.Mode.SRC_ATOP);
            //getSupportActionBar().setIcon(icon);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void irAlMapa(String idColocacion) {
        idColocacionCreada = idColocacion;
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_mostrar_colocadas);
        onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_mostrar_colocadas));
    }
}
