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
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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


public class ExtraerTrampa extends Fragment {
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

    @BindView(R.id.tempMin)
    TextView tvTempMin;

    @BindView(R.id.tempMax)
    TextView tvTempMax;

    @BindView(R.id.tempProm)
    TextView tvTempProm;

    @BindView(R.id.humMin)
    TextView tvHumMin;

    @BindView(R.id.humMax)
    TextView tvHumMax;

    @BindView(R.id.humProm)
    TextView tvHumProm;

    @BindView(R.id.llProgressBarLista)
    LinearLayout llProgressBarLista;

    @BindView(R.id.llProgressBarDatos)
    LinearLayout getLlProgressBarDatos;

    @BindView(R.id.tvProgressBarDatos)
    TextView tvProgressBarDatos;

    @BindView(R.id.btnExtraer)
    Button btnExtraer;

    @BindView(R.id.datosRecibidos)
    TextView tvDatosRecibidos;

    List<Trampa> trampas;

    Trampa trampa = null;  //Trampa seleccionada

    //Datos obtenidos de la trampa
    float tempMin = 0;
    float tempMax = 0;
    float humMin = 0;
    float humMax = 0;
    float promH = 0;
    float promT = 0;

    private Handler mHandler;
    private ConnectedThread mConnectedThread;
    private BluetoothSocket mBTSocket = null;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier
    private final static int REQUEST_ENABLE_BT = 1;
    private final static int MESSAGE_READ = 2;
    private final static int CONNECTING_STATUS = 3;

    private Context mContext;

    public ExtraerTrampa() {
    }


    @SuppressLint("HandlerLeak")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_extraer_trampa, container, false);
        ButterKnife.bind(this, v);

        mBTArrayAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio

        listaDispositivos.setAdapter(mBTArrayAdapter); // assign model to view
        listaDispositivos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                     @Override
                                                     public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

                                                         if (!mBTAdapter.isEnabled()) {
                                                             Toast.makeText(mContext, R.string.bluetooth_apagado, Toast.LENGTH_SHORT).show();
                                                             return;
                                                         }

                                                         tvEstado.setText(R.string.conectando);
                                                         tvEstado.setTextColor(mContext.getResources().getColor(R.color.colorGris));
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
                                                                     Toast.makeText(mContext, "Socket creation failed", Toast.LENGTH_SHORT).show();
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
                                                                         Toast.makeText(mContext, "Socket creation failed", Toast.LENGTH_SHORT).show();
                                                                     }
                                                                 }
                                                                 if (!fail) {
                                                                     if (trampas != null)
                                                                         for (Trampa t : trampas)
                                                                             if (t.getMac().equals(address))
                                                                                 trampa = t;
                                                                     mConnectedThread = new ConnectedThread(mBTSocket, mHandler, MESSAGE_READ);
                                                                     mConnectedThread.start();
                                                                     mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name).sendToTarget();
                                                                 }
                                                             }
                                                         }.start();
                                                     }
                                                 }
        );


        mHandler = new Handler() {
            @SuppressLint("SetTextI18n")
            @Override
            public void handleMessage(android.os.Message msg) {
                if (msg.what == MESSAGE_READ) {
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    if (readMessage.contains("Tmax") && readMessage.contains("Tmin")) {
                        try {
                            String[] splitT = readMessage.split("T");
                            tempMin = Float.valueOf(splitT[2].substring(3, 8));
                            tempMax = Float.valueOf(splitT[1].substring(3, 8));
                        } catch (NumberFormatException nfe) {
                            mConnectedThread.write("c"); //Si el dato está corrupto volver a perdirlo.
                        }

                        //Delay de 100ms
                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        mConnectedThread.write("d"); //Obtener dato de Humedad
                                    }
                                },
                                100);

                    } else if (readMessage.contains("Hmax") && readMessage.contains("Hmin")) {
                        try {
                            String[] splitH = readMessage.split("H");
                            humMin = Float.valueOf(splitH[2].substring(3, 8));
                            humMax = Float.valueOf(splitH[1].substring(3, 8));
                        } catch (NumberFormatException nfe) {
                            mConnectedThread.write("d"); //Si el dato está corrupto volver a perdirlo.
                        }

                        //Delay de 100ms
                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        mConnectedThread.write("b"); //Obtener dato de Promedio de Temperatura y Humedad.
                                    }
                                },
                                100);

                    } else if (readMessage.contains("H") && readMessage.contains("T")) {

                        try {
                            promH = Float.valueOf(readMessage.substring(1, 5));
                            promT = Float.valueOf(readMessage.substring(7, 11));

                        } catch (NumberFormatException nfe) {
                            mConnectedThread.write("b"); //Si el dato está corrupto volver a perdirlo.
                        }
                        mConnectedThread.cancel();

                        tvEstado.setText(R.string.datos_recibidos);
                        tvEstado.setTextColor(mContext.getResources().getColor(R.color.colorVerde));

                        //Mostrar datos en los TextViews
                        tvDatosRecibidos.setText("Datos de " + trampa.getNombre() + " recibidos");
                        tvTempMin.setText(String.valueOf(tempMin) + "°C");
                        tvTempMax.setText(String.valueOf(tempMax) + "°C");
                        tvHumMin.setText(String.valueOf(humMin) + "%");
                        tvHumMax.setText(String.valueOf(humMax) + "%");
                        tvHumProm.setText(String.valueOf(promH) + "%");
                        tvTempProm.setText(String.valueOf(promT) + "°C");
                        getLlProgressBarDatos.setVisibility(View.GONE);
                    }
                }

                if (msg.what == CONNECTING_STATUS) {
                    if (msg.arg1 == 1) {
                        String estado = "Conectado al dispositivo: " + (String) (msg.obj);
                        tvEstado.setText(estado.trim());
                        tvEstado.setTextColor(mContext.getResources().getColor(R.color.colorVerde));
                        getLlProgressBarDatos.setVisibility(View.VISIBLE);
                        mConnectedThread.write("c"); //Obtener dato de Temperatura.
                    } else {
                        tvEstado.setText(R.string.conexion_fallo);
                        tvEstado.setTextColor(mContext.getResources().getColor(R.color.colorRojo));
                    }

                }
            }

        };

        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            tvEstado.setText(R.string.bluetooth_no_encontrado);
            tvEstado.setTextColor(mContext.getResources().getColor(R.color.colorRojo));
            Toast.makeText(mContext, R.string.bluetooth_no_encontrado, Toast.LENGTH_SHORT).show();
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

        btnExtraer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                extraerTrampa();
            }
        });

        cargarTrampas();
        return v;
    }

    private void extraerTrampa() {
        if (trampa != null && tempMax != 0 && tempMin != 0 && humMax != 0 && humMin != 0 && promH != 0 && promT != 0) {

            tvProgressBarDatos.setText(R.string.almacenando_datos);
            getLlProgressBarDatos.setVisibility(View.VISIBLE);
            BDInterface bd = BDCliente.getClient().create(BDInterface.class);
            Call<Respuesta> call = bd.extraerTrampa(trampa.getId(), tempMin, tempMax, humMin, humMax, promH, promT);
            call.enqueue(new Callback<Respuesta>() {
                @Override
                public void onResponse(Call<Respuesta> call, Response<Respuesta> response) {
                    tvProgressBarDatos.setText(R.string.obteniendo_datos);
                    getLlProgressBarDatos.setVisibility(View.GONE);
                    if (response.body() != null) {
                        if (response.body().getCodigo().equals("1")) {
                            Snackbar.make(((MenuPrincipal) mContext).getWindow().getDecorView().getRootView(), response.body().getMensaje(), Snackbar.LENGTH_LONG).show();
                            trampa = null;
                            tvDatosRecibidos.setText(R.string.datos_recibidos);
                            tvTempMin.setText("");
                            tvTempMax.setText("");
                            tvHumMin.setText("");
                            tvHumMax.setText("");
                            tvHumProm.setText("");
                            tvTempProm.setText("");
                            cargarTrampas();
                        } else {
                            Toast.makeText(mContext, response.body().getMensaje(), Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        Toast.makeText(mContext, R.string.error_interno_servidor, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Respuesta> call, Throwable t) {
                    tvProgressBarDatos.setText(R.string.obteniendo_datos);
                    getLlProgressBarDatos.setVisibility(View.GONE);
                    Toast.makeText(mContext, R.string.error_conexion_servidor, Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(mContext, "Antes debe de seleccionar una trampa", Toast.LENGTH_SHORT).show();
        }
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
        } else {
            btnConectarBT.setBackgroundResource(R.drawable.ic_bluetooth_conectado);
            tvEstado.setText(R.string.bluetooth_encendido);
            tvEstado.setTextColor(mContext.getResources().getColor(R.color.colorVerde));
            dispositivosVinculados();
        }
    }

    // Enter here after user selects "yes" or "no" to enabling radio

    public void onActivityResult(int requestCode, int resultCode, Intent Data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == ((MenuPrincipal) mContext).RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                tvEstado.setText(R.string.bluetooth_encendido);
                tvEstado.setTextColor(mContext.getResources().getColor(R.color.colorVerde));
                btnConectarBT.setBackgroundResource(R.drawable.ic_bluetooth_conectado);
                dispositivosVinculados();
            } else {
                tvEstado.setText(R.string.bluetooth_apagado);
                tvEstado.setTextColor(mContext.getResources().getColor(R.color.colorRojo));
            }
            llProgressBarLista.setVisibility(View.GONE);
        }
    }


    private void apagarBT() {
        mBTAdapter.disable();
        tvEstado.setText(R.string.bluetooth_apagado);
        tvEstado.setTextColor(mContext.getResources().getColor(R.color.colorRojo));
        btnConectarBT.setBackgroundResource(R.drawable.ic_bluetooth);
        Toast.makeText(mContext, R.string.bluetooth_apagado, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(mContext, R.string.escaneo_detenido, Toast.LENGTH_SHORT).show();
            llProgressBarLista.setVisibility(View.GONE);
        } else {
            if (mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                llProgressBarLista.setVisibility(View.VISIBLE);

                //Ocultar progressBar despues de 12(tiempo que escanea la función startDiscovery() ) segundos si no se encontró ningún dipositivo bt.
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (llProgressBarLista.getVisibility() == View.VISIBLE)
                            llProgressBarLista.setVisibility(View.GONE);
                    }
                }, 12000);

                Toast.makeText(mContext, R.string.escaneando, Toast.LENGTH_SHORT).show();
                tvTituloLista.setText(R.string.trampas_encontradas);
                mContext.registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            } else {
                Toast.makeText(mContext, R.string.bluetooth_apagado, Toast.LENGTH_SHORT).show();
            }
        }
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            llProgressBarLista.setVisibility(View.GONE);
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                Trampa trampa = null;
                if (trampas != null) {
                    for (Trampa t : trampas) {
                        if (t.getMac().equals(device.getAddress())) {
                            trampa = t;
                        }
                    }
                }
                if (trampa != null) {
                    mBTArrayAdapter.add(trampa.getNombre() + "\n" + device.getAddress());
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
                if (trampas != null) {
                    Trampa trampa = null;
                    for (Trampa t : trampas) {
                        if (t.getMac().equals(device.getAddress())) {
                            trampa = t;
                        }

                    }
                    if (trampa != null)
                        mBTArrayAdapter.add(trampa.getNombre() + "\n" + device.getAddress());
                }
            }

            tvTituloLista.setText(R.string.trampas_vinculadas);
            llProgressBarLista.setVisibility(View.GONE);
        } else
            Toast.makeText(mContext, R.string.bluetooth_apagado, Toast.LENGTH_SHORT).show();
    }

    private void cargarTrampas() {
        llProgressBarLista.setVisibility(View.VISIBLE);
        BDInterface bd = BDCliente.getClient().create(BDInterface.class);
        Call<RespuestaTrampas> call = bd.obtenerTrampasColocadas();
        call.enqueue(new Callback<RespuestaTrampas>() {
            @Override
            public void onResponse(Call<RespuestaTrampas> call, Response<RespuestaTrampas> response) {
                if (response.body() != null) {
                    if (response.body().getCodigo().equals("1")) {
                        trampas = response.body().getTrampas();
                        encenderBT();
                    } else {
                        trampas = null;
                        Toast.makeText(mContext, response.body().getMensaje(), Toast.LENGTH_SHORT).show();
                        llProgressBarLista.setVisibility(View.GONE);
                    }
                } else {
                    trampas = null;
                    Toast.makeText(mContext, R.string.error_interno_servidor, Toast.LENGTH_SHORT).show();
                    llProgressBarLista.setVisibility(View.GONE);

                }
            }

            @Override
            public void onFailure(Call<RespuestaTrampas> call, Throwable t) {
                trampas = null;
                Toast.makeText(mContext, R.string.error_conexion_servidor, Toast.LENGTH_SHORT).show();
                llProgressBarLista.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mConnectedThread != null)
            mConnectedThread.cancel();
    }
}

