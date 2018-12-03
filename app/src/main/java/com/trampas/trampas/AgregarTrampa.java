package com.trampas.trampas;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.trampas.trampas.BD.BDCliente;
import com.trampas.trampas.BD.BDInterface;
import com.trampas.trampas.BD.Respuesta;
import com.trampas.trampas.BD.RespuestaTrampas;
import com.trampas.trampas.Bluetooth.ConnectedThread;
import com.trampas.trampas.Clases.Trampa;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgregarTrampa extends AppCompatActivity {
    private BluetoothAdapter mBTAdapter;
    private ArrayAdapter<String> mBTArrayAdapter;

    @BindView(R.id.btnConectarBT)
    Button btnConectarBT;

    @BindView(R.id.btnDispositivosVinculados)
    Button btnDispositivosVinculados;

    @BindView(R.id.btnEscanear)
    Button btnEscanear;

    @BindView(R.id.tvTituloLista)
    TextView tvTituloLista;

    @BindView(R.id.listaDispositivos)
    ListView listaDispositivos;

    @BindView(R.id.tvEstado)
    TextView tvEstado;

    @BindView(R.id.etNombre)
    EditText etNombre;

    @BindView(R.id.etNombreError)
    TextInputLayout etNombreError;

    @BindView(R.id.llProgressBarLista)
    LinearLayout llProgressBarLista;

    @BindView(R.id.llProgressBarDatos)
    LinearLayout llProgressBarDatos;

    @BindView(R.id.btnAgregar)
    Button btnAgregar;

    List<Trampa> trampas;

    String mac = null;  //Direcccion mac de la trampa seleccionada

    //Datos obtenidos de la trampa
    String nombre = null;

    private Handler mHandler;
    private ConnectedThread mConnectedThread;
    private BluetoothSocket mBTSocket = null;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier
    private final static int REQUEST_ENABLE_BT = 1;
    private final static int MESSAGE_READ = 2;
    private final static int CONNECTING_STATUS = 3;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_trampa);

        //Cambiar titulo de la barra superior.
        ActionBar barra = getSupportActionBar();
        if (barra != null)
            barra.setTitle("Agregar trampa");

        //Evitar que el teclado se abra cuando se inicia la actividad.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        ButterKnife.bind(this);

        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); //Obtener un adaptador para el bt.
        mBTArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1); //Crear un adaptador simple para la lista de dispositivos bt.
        listaDispositivos.setAdapter(mBTArrayAdapter); //Setear adaptador a la lista.

        //Setear los eventos a realizar al seleccionar un dispositivo bt.
        listaDispositivos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                     @Override
                                                     public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

                                                         if (!mBTAdapter.isEnabled()) {
                                                             Toast.makeText(AgregarTrampa.this, R.string.bluetooth_apagado, Toast.LENGTH_SHORT).show();
                                                             return;
                                                         }

                                                         tvEstado.setText(R.string.conectando);

                                                         //Se obtiene la mac del dispositivo(últimos 17 caracteres).
                                                         String info = ((TextView) v).getText().toString();
                                                         final String address = info.substring(info.length() - 17);
                                                         final String name = info.substring(0, info.length() - 17);

                                                         //Se crea un nuevo hilo para no bloquear el GUI
                                                         new Thread() {
                                                             public void run() {
                                                                 boolean fail = false;
                                                                 BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                                                                 //Se crea un socket de conexión bt.
                                                                 try {
                                                                     mBTSocket = createBluetoothSocket(device);
                                                                 } catch (IOException e) {
                                                                     fail = true;
                                                                     Toast.makeText(AgregarTrampa.this, "Falló la creación del socket", Toast.LENGTH_SHORT).show();
                                                                 }

                                                                 //Se conecta el socket.
                                                                 try {
                                                                     mBTSocket.connect();
                                                                 } catch (IOException e) {
                                                                     try {
                                                                         fail = true;
                                                                         mBTSocket.close();
                                                                         mHandler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                                                                     } catch (IOException e2) {
                                                                         Toast.makeText(AgregarTrampa.this, "Falló la creación del socket", Toast.LENGTH_SHORT).show();
                                                                     }
                                                                 }

                                                                 //Si la conexión fue exitosa.
                                                                 if (!fail) {
                                                                     //Se crea un hilo para menejar la conexión.
                                                                     mConnectedThread = new ConnectedThread(mBTSocket, mHandler, MESSAGE_READ);
                                                                     mConnectedThread.start();

                                                                     //Se guarda la mac del dispositivo conectado.
                                                                     mac = address;

                                                                     //Se llama al handler del hilo para indicar al GUI que se conectó el dispositivo.
                                                                     mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name).sendToTarget();
                                                                 }
                                                             }
                                                         }.start();
                                                     }
                                                 }
        );

        //Se crea un handler para manejar los dos hilos.
        mHandler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                if (msg.what == CONNECTING_STATUS) {
                    if (msg.arg1 == 1) {
                        //Si se conectó correctamente con el dispositivo.
                        String estado = getString(R.string.conectado) + (msg.obj);
                        tvEstado.setText(estado.trim());

                        etNombreError.setVisibility(View.GONE);
                        llProgressBarDatos.setVisibility(View.VISIBLE);

                        //Solicitar a la trampa su nombre
                        mConnectedThread.write("a");
                    } else {
                        tvEstado.setText(R.string.conexion_fallo);
                    }

                }

                //Si se recibió un mensaje desde el dispositivo (enviado por el hilo ConnectedThread).
                if (msg.what == MESSAGE_READ) {
                    //Leer el mensaje.
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        //Si falla al leer el mensaje volver a solicitar.
                        e.printStackTrace();
                        mConnectedThread.write("a");
                    }

                    //Si se pudo leer correctamente.
                    if (readMessage != null) {
                        nombre = readMessage.substring(0, 7);
                        etNombre.setText(nombre);
                        //Desconectar dispositivo.
                        mConnectedThread.cancel();

                        tvEstado.setText(R.string.datos_recibidos);
                        llProgressBarDatos.setVisibility(View.GONE);
                        etNombreError.setVisibility(View.VISIBLE);
                    }
                }
            }
        };

        //Si el teléfono no soporta bt.
        if (mBTArrayAdapter == null) {
            tvEstado.setText(R.string.bluetooth_no_encontrado);
            Toast.makeText(this, R.string.bluetooth_no_encontrado, Toast.LENGTH_SHORT).show();
        } else {
            //Sino, setear eventos para manejar el bt.
            btnConectarBT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controlarBT();
                }
            });
            btnDispositivosVinculados.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispositivosVinculados();
                }
            });

            btnEscanear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    escanear();
                }
            });
        }

        //Setear evento para agregar la trampa.
        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarTrampa();
            }
        });

        cargarTrampas();
    }

    //Agregar trampa con el nombre del EditText "nombreTrampa" y la mac guardada.
    public void agregarTrampa() {
        if (mac == null) {
            Toast.makeText(this, R.string.debe_de_seleccionar_un_dispositivo_para_agregar, Toast.LENGTH_SHORT).show();
            return;
        }

        final String nombreTrampa = etNombre.getText().toString().trim();
        etNombreError.setError("");

        if (nombreTrampa.equals("")) {
            etNombreError.setError(getString(R.string.campo_requerido));
            return;
        } else if (nombreTrampa.length() > 250) {
            etNombreError.setError("El nombre es demasiado largo.");
            return;
        }

        final Intent menu = getParentActivityIntent();

        BDInterface bd = BDCliente.getClient().create(BDInterface.class);
        Call<Respuesta> call = bd.agregarTrampa(nombreTrampa, mac);
        call.enqueue(new Callback<Respuesta>() {
            @Override
            public void onResponse(Call<Respuesta> call, Response<Respuesta> response) {
                if (response.body() != null) {
                    if (response.body().getCodigo().equals("-1")) {
                        etNombreError.setError(response.body().getMensaje());
                    } else {
                        etNombre.setText("");
                        Snackbar.make(getWindow().getDecorView().getRootView(), response.body().getMensaje(), Snackbar.LENGTH_LONG).show();
                        startActivity(menu);
                        //hideKeyboard();
                    }
                } else {
                    Toast.makeText(AgregarTrampa.this, R.string.error_interno_servidor, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Respuesta> call, Throwable t) {
                Toast.makeText(AgregarTrampa.this, R.string.error_conexion_servidor, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Crear socket para conexión bt.
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    //Encender bluetooth
    private void encenderBT() {
        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            tvEstado.setText(R.string.bluetooth_encendido);
            btnConectarBT.setBackgroundResource(R.drawable.ic_bluetooth_conectado);
            dispositivosVinculados();

        }
    }

    //Se entra acá cuando el usuario decide o no encender el bt.
    public void onActivityResult(int requestCode, int resultCode, Intent Data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            //Si el usuario aceptó la solicitud de enceder bt.
            if (resultCode == RESULT_OK) {
                tvEstado.setText(R.string.bluetooth_encendido);
                //Cambiar a icono encendido.
                btnConectarBT.setBackgroundResource(R.drawable.ic_bluetooth_conectado);
                dispositivosVinculados();
            } else
                tvEstado.setText(R.string.bluetooth_apagado);
            llProgressBarLista.setVisibility(View.GONE);
        }
    }

    //Apagar bluetooth.
    private void apagarBT() {
        mBTAdapter.disable();
        tvEstado.setText(R.string.bluetooth_apagado);
        btnConectarBT.setBackgroundResource(R.drawable.ic_bluetooth);
        Toast.makeText(this, "Bluetooth apagado", Toast.LENGTH_SHORT).show();
    }

    //Manejar bluetooth.
    private void controlarBT() {
        if (mBTAdapter.isEnabled())
            apagarBT();
        else
            encenderBT();

    }

    private void escanear() {
        //Si ya esta escaneando, detener.
        if (mBTAdapter.isDiscovering()) {
            mBTAdapter.cancelDiscovery();
            Toast.makeText(this, R.string.escaneo_detenido, Toast.LENGTH_SHORT).show();
            llProgressBarLista.setVisibility(View.GONE);
        } else {
            //Sino, si el bt esta encendido comenzar a escanear.
            if (mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                llProgressBarLista.setVisibility(View.VISIBLE);

                //Ocultar progressBar despues de 12 segundos( tiempo que escanea la función startDiscovery() )  si no se encontró ningún dipositivo bt.
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (llProgressBarLista.getVisibility() == View.VISIBLE)
                            llProgressBarLista.setVisibility(View.GONE);
                    }
                }, 12000);

                Toast.makeText(this, R.string.escaneando, Toast.LENGTH_SHORT).show();
                tvTituloLista.setText(R.string.dispositivos_encontrados);

                //Registrar recibidor para que sea llamado al encontrar dispositivos.
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            } else {
                Toast.makeText(this, R.string.bluetooth_apagado, Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Se crea recibidor para meanjer los dispositivos encontrados.
    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            llProgressBarLista.setVisibility(View.GONE);
            //Si se encontró algún dispositivo bt.
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                //Verificar que el dispositivo no esté ya agregado como trampa.
                Boolean agregar = true;
                if (trampas != null) {
                    for (Trampa t : trampas) {
                        if (t.getMac().equals(device.getAddress()))
                            agregar = false;
                    }
                }

                if (agregar) {
                    mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    mBTArrayAdapter.notifyDataSetChanged();
                }

            }
        }
    };

    //Listar dispositivos bt vinculados al teléfono.
    private void dispositivosVinculados() {
        llProgressBarLista.setVisibility(View.VISIBLE);
        Set<BluetoothDevice> mPairedDevices = mBTAdapter.getBondedDevices();
        if (mBTAdapter.isEnabled()) {
            mBTArrayAdapter.clear();
            for (BluetoothDevice device : mPairedDevices) {
                Boolean agregar = true;
                if (trampas != null) {
                    for (Trampa t : trampas) {
                        if (t.getMac().equals(device.getAddress()))
                            agregar = false;
                    }
                }
                if (agregar)
                    mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            }
            tvTituloLista.setText(R.string.dispositivos_vinculados);
            llProgressBarLista.setVisibility(View.GONE);
        } else {
            Toast.makeText(this, R.string.bluetooth_apagado, Toast.LENGTH_SHORT).show();
            llProgressBarLista.setVisibility(View.GONE);
        }

    }

    //Obtener trampas desde el servidor.
    private void cargarTrampas() {
        BDInterface bd = BDCliente.getClient().create(BDInterface.class);
        Call<RespuestaTrampas> call = bd.obtenerTrampas();
        call.enqueue(new Callback<RespuestaTrampas>() {
            @Override
            public void onResponse(Call<RespuestaTrampas> call, Response<RespuestaTrampas> response) {
                if (response.body() != null) {
                    if (response.body().getCodigo().equals("1")) {
                        trampas = response.body().getTrampas();
                        encenderBT();
                    } else {
                        trampas = null;
                        Toast.makeText(AgregarTrampa.this, response.body().getMensaje(), Toast.LENGTH_SHORT).show();
                        llProgressBarLista.setVisibility(View.GONE);
                    }
                } else {
                    trampas = null;
                    Toast.makeText(AgregarTrampa.this, R.string.error_interno_servidor, Toast.LENGTH_SHORT).show();
                    llProgressBarLista.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<RespuestaTrampas> call, Throwable t) {
                trampas = null;
                Toast.makeText(AgregarTrampa.this, R.string.error_conexion_servidor, Toast.LENGTH_SHORT).show();
                llProgressBarLista.setVisibility(View.GONE);
            }

        });
    }
}
