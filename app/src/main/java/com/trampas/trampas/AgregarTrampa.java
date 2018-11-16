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
import android.support.design.widget.TextInputLayout;
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
    private Set<BluetoothDevice> mPairedDevices;
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

    private Handler mHandler; // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names

    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update

    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_trampa);
        getSupportActionBar().setTitle("Agregar trampa");
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        ButterKnife.bind(this);

        mBTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio

        listaDispositivos.setAdapter(mBTArrayAdapter); // assign model to view
        listaDispositivos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                     @Override
                                                     public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

                                                         if (!mBTAdapter.isEnabled()) {
                                                             Toast.makeText(AgregarTrampa.this, "Bluetooth apagado", Toast.LENGTH_SHORT).show();
                                                             return;
                                                         }

                                                         tvEstado.setText("Conectando...");
                                                         // Get the device MAC address, which is the last 17 chars in the View
                                                         String info = ((TextView) v).getText().toString();
                                                         final String address = info.substring(info.length() - 17);
                                                         final String name = info.substring(0, info.length() - 17);

                                                         // Spawn a new thread to avoid blocking the GUI one
                                                         new Thread() {
                                                             public void run() {
                                                                 boolean fail = false;
                                                                 BluetoothDevice device = mBTAdapter.getRemoteDevice(address);
                                                                 try {
                                                                     mBTSocket = createBluetoothSocket(device);
                                                                 } catch (IOException e) {
                                                                     fail = true;
                                                                     Toast.makeText(AgregarTrampa.this, "Socket creation failed", Toast.LENGTH_SHORT).show();
                                                                 }
                                                                 // Establish the Bluetooth socket connection.
                                                                 try {
                                                                     mBTSocket.connect();
                                                                 } catch (IOException e) {
                                                                     try {
                                                                         fail = true;
                                                                         mBTSocket.close();
                                                                         mHandler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                                                                     } catch (IOException e2) {
                                                                         //insert code to deal with this
                                                                         Toast.makeText(AgregarTrampa.this, "Socket creation failed", Toast.LENGTH_SHORT).show();
                                                                     }
                                                                 }
                                                                 if (fail == false) {
                                                                     mConnectedThread = new ConnectedThread(mBTSocket, mHandler, MESSAGE_READ);
                                                                     mConnectedThread.start();
                                                                     mac = address;
                                                                     mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name).sendToTarget();
                                                                 }
                                                             }
                                                         }.start();
                                                     }
                                                 }
        );


        mHandler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                if (msg.what == MESSAGE_READ) {
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    nombre = readMessage.substring(0, 7);
                    etNombre.setText(nombre);
                    mConnectedThread.cancel();
                    llProgressBarDatos.setVisibility(View.GONE);
                    etNombreError.setVisibility(View.VISIBLE);
                    tvEstado.setText("Desconectado, datos recibidos");
                }


                if (msg.what == CONNECTING_STATUS) {
                    if (msg.arg1 == 1) {
                        String estado = "Conectado al dispositivo: " + (String) (msg.obj);
                        tvEstado.setText(estado.trim());
                        llProgressBarDatos.setVisibility(View.VISIBLE);
                        etNombreError.setVisibility(View.GONE);
                        mConnectedThread.write("a"); //Obtener nombre
                    } else {
                        tvEstado.setText("La conexión falló, reintente.");
                    }

                }
            }
        };

        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            tvEstado.setText("Bluetooth no encontrado");
            Toast.makeText(this, "Dispositivo Bluetooth no encontrado!", Toast.LENGTH_SHORT).show();
        } else {

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

        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarTrampa();
            }
        });

        llProgressBarLista.setVisibility(View.VISIBLE);
        cargarTrampas();
    }


    public void agregarTrampa() {
        if (mac == null) {
            Toast.makeText(this, "Debe de seleccionar un dispositivo para agregar", Toast.LENGTH_SHORT).show();
            return;
        }

        final String nombreTrampa = etNombre.getText().toString().trim();
        etNombreError.setError("");

        if (nombreTrampa.equals("")) {
            etNombreError.setError("El nombre no puede quedar vacío.");
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
                        Toast.makeText(AgregarTrampa.this, response.body().getMensaje(), Toast.LENGTH_SHORT).show();
                        startActivity(menu);
                        //hideKeyboard();
                    }
                } else {
                    Toast.makeText(AgregarTrampa.this, "Error interno del servidor, Reintente", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Respuesta> call, Throwable t) {
                Toast.makeText(AgregarTrampa.this, "Error de conexión con el servidor: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    //Encender bluetooth
    private void encenderBT() {
        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            tvEstado.setText("Bluetooth encendido");
            Toast.makeText(this, "Encendiendo Bluetooth...", Toast.LENGTH_SHORT).show();
        } else {
            btnConectarBT.setBackgroundResource(R.drawable.ic_bluetooth_conectado);
            dispositivosVinculados();

        }
    }

    // Enter here after user selects "yes" or "no" to enabling radio
    public void onActivityResult(int requestCode, int resultCode, Intent Data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                tvEstado.setText("Bluetooth encendido");
                btnConectarBT.setBackgroundResource(R.drawable.ic_bluetooth_conectado);
                dispositivosVinculados();
            } else
                tvEstado.setText("Bluetooth apagado");
            llProgressBarLista.setVisibility(View.GONE);
        }
    }


    private void apagarBT() {
        mBTAdapter.disable(); // turn off
        tvEstado.setText("Bluetooth apagado");
        btnConectarBT.setBackgroundResource(R.drawable.ic_bluetooth);
        Toast.makeText(this, "Bluetooth apagado", Toast.LENGTH_SHORT).show();
    }


    private void controlarBT() {
        if (mBTAdapter.isEnabled())
            apagarBT();
        else
            encenderBT();

    }

    private void escanear() {
        // Check if the device is already discovering
        if (mBTAdapter.isDiscovering()) {
            mBTAdapter.cancelDiscovery();
            Toast.makeText(this, "Escaneo detenido", Toast.LENGTH_SHORT).show();
        } else {
            if (mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                Toast.makeText(this, "Escaneo iniciado", Toast.LENGTH_SHORT).show();
                tvTituloLista.setText("Trampas encontradas");
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            } else {
                Toast.makeText(this, "Bluetooth apagado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list

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

    private void dispositivosVinculados() {
        mPairedDevices = mBTAdapter.getBondedDevices();
        if (mBTAdapter.isEnabled()) {
            // put it's one to the adapter
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
            tvTituloLista.setText("Dispositivos vinculados");
            llProgressBarLista.setVisibility(View.GONE);
        } else
            Toast.makeText(this, "Bluetooth apagado", Toast.LENGTH_SHORT).show();
    }

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
                    Toast.makeText(AgregarTrampa.this, "Error interno del servidor, Reintente", Toast.LENGTH_SHORT).show();
                    llProgressBarLista.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<RespuestaTrampas> call, Throwable t) {
                trampas = null;
                Toast.makeText(AgregarTrampa.this, "Error de conexión con el servidor: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                llProgressBarLista.setVisibility(View.GONE);
            }

        });
    }
}
