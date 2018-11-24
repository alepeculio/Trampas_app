package com.trampas.trampas;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.trampas.trampas.BD.BDCliente;
import com.trampas.trampas.BD.BDInterface;
import com.trampas.trampas.BD.RespuestaColocaciones;
import com.trampas.trampas.BD.RespuestaTrampas;
import com.trampas.trampas.Clases.Colocacion;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ColocacionGrafica extends AppCompatActivity {
    Colocacion colocacion;

    @BindView(R.id.graphHum)
    GraphView graphHum;

    @BindView(R.id.graphTemp)
    GraphView graphTemp;

    @BindView(R.id.tvNombre)
    TextView tvNombre;

    @BindView(R.id.btnMasInformacion)
    LinearLayout btnMasInformacion;

    List<Colocacion> colocaciones;

    public void setColocacion(Colocacion colocacion) {
        this.colocacion = colocacion;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colocacion_grafica);

        colocacion = (Colocacion) getIntent().getSerializableExtra("colocacion");
        ButterKnife.bind(this);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Gráfica");
        tvNombre.setText(colocacion.getTrampa().getNombre() + "(periodo " + colocacion.getPeriodo() + ")");

        btnMasInformacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ColocacionGrafica.this, DatosTrampa.class);
                intent.putExtra("trampa", colocacion.getTrampa());
                intent.putExtra("periodo", colocacion.getPeriodo());
                startActivity(intent);

            }
        });

        //Graficas
        cargarPeriodo(colocacion.getPeriodo());

    }

    public void prepararGraficaTemp() {
        if (graphTemp.getSeries().size() == 0 && colocaciones != null) {

            double temp1 = 500;
            double temp2 = 500;
            double temp3 = 500;

            String etiqueta1 = "Día 1";
            String etiqueta2 = "Día 2";
            String etiqueta3 = "Día 3";
            String sinDatos = " (sin datos)";

            try {
                Colocacion c1 = colocaciones.get(0);
                if (c1 != null)
                    temp1 = Double.valueOf(c1.getTempProm());

                Colocacion c2 = colocaciones.get(1);
                if (c2 != null)
                    temp2 = Double.valueOf(c2.getTempProm());

                Colocacion c3 = colocaciones.get(2);
                if (c3 != null)
                    temp3 = Double.valueOf(c3.getTempProm());

            } catch (IndexOutOfBoundsException iobe) {
                iobe.printStackTrace();
            }

            if (temp1 == 500) {
                etiqueta1 += sinDatos;
                temp1 = 0;
            }

            if (temp2 == 500) {
                etiqueta2 += sinDatos;
                temp2 = 0;
            }

            if (temp3 == 500) {
                etiqueta3 += sinDatos;
                temp3 = 0;
            }

            LineGraphSeries<DataPoint> seriesTemp = new LineGraphSeries<>(new DataPoint[]{
                    new DataPoint(1, temp1),
                    new DataPoint(2, temp2),
                    new DataPoint(3, temp3),
            });

            //Estilo
            seriesTemp.setDrawDataPoints(true);
            seriesTemp.setDataPointsRadius(10);
            seriesTemp.setDrawBackground(true);
            seriesTemp.setColor(Color.argb(255, 255, 60, 60));
            seriesTemp.setBackgroundColor(Color.argb(100, 204, 119, 119));
            seriesTemp.setAnimated(true);
            seriesTemp.setThickness(8);
            graphTemp.addSeries(seriesTemp);

            //Estilo de etiquetas
            graphTemp.getGridLabelRenderer().setHighlightZeroLines(false);
            graphTemp.getGridLabelRenderer().setVerticalLabelsAlign(Paint.Align.LEFT);
            graphTemp.getGridLabelRenderer().setLabelVerticalWidth(100);
            graphTemp.getGridLabelRenderer().setTextSize(20);
            graphTemp.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
            graphTemp.getGridLabelRenderer().reloadStyles();

            //Cambiar las etiquetas
            StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graphTemp);
            staticLabelsFormatter.setDynamicLabelFormatter(new DefaultLabelFormatter() {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (!isValueX)
                        return super.formatLabel(value, isValueX) + " °C";
                    else
                        return super.formatLabel(value, isValueX);
                }
            });
            staticLabelsFormatter.setHorizontalLabels(new String[]{etiqueta1, etiqueta2, etiqueta3});
            graphTemp.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        }
    }

    public void prepararGraficaHum() {
        if (graphHum.getSeries().size() == 0 && colocaciones != null) {

            double hum1 = 500;
            double hum2 = 500;
            double hum3 = 500;

            String etiqueta1 = "Día 1";
            String etiqueta2 = "Día 2";
            String etiqueta3 = "Día 3";
            String sinDatos = " (sin datos)";

            try {
                Colocacion c1 = colocaciones.get(0);
                if (c1 != null)
                    hum1 = Double.valueOf(c1.getHumProm());

                Colocacion c2 = colocaciones.get(1);
                if (c2 != null)
                    hum2 = Double.valueOf(c2.getHumProm());

                Colocacion c3 = colocaciones.get(2);
                if (c3 != null)
                    hum3 = Double.valueOf(c3.getHumProm());

            } catch (IndexOutOfBoundsException iobe) {
                iobe.printStackTrace();
            }

            if (hum1 == 500) {
                etiqueta1 += sinDatos;
                hum1 = 0;
            }

            if (hum2 == 500) {
                etiqueta2 += sinDatos;
                hum2 = 0;
            }

            if (hum3 == 500) {
                etiqueta3 += sinDatos;
                hum3 = 0;
            }

            LineGraphSeries<DataPoint> seriesHum = new LineGraphSeries<>(new DataPoint[]{
                    new DataPoint(1, hum1),
                    new DataPoint(2, hum2),
                    new DataPoint(3, hum3),
            });

            //Estilo
            seriesHum.setDrawDataPoints(true);
            seriesHum.setDataPointsRadius(10);
            seriesHum.setDrawBackground(true);
            seriesHum.setColor(Color.argb(255, 66, 95, 244));
            seriesHum.setBackgroundColor(Color.argb(100, 66, 95, 244));
            seriesHum.setAnimated(true);
            seriesHum.setThickness(8);
            graphHum.addSeries(seriesHum);

            //Estilo de etiquetas
            graphHum.getGridLabelRenderer().setHighlightZeroLines(false);
            graphHum.getGridLabelRenderer().setVerticalLabelsAlign(Paint.Align.LEFT);
            graphHum.getGridLabelRenderer().setLabelVerticalWidth(100);
            graphHum.getGridLabelRenderer().setTextSize(20);
            graphHum.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
            graphHum.getGridLabelRenderer().reloadStyles();

            //Cambiar las etiquetas
            StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graphHum);
            staticLabelsFormatter.setDynamicLabelFormatter(new DefaultLabelFormatter() {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (!isValueX)
                        return super.formatLabel(value, isValueX) + " %";
                    else
                        return super.formatLabel(value, isValueX);
                }
            });
            staticLabelsFormatter.setHorizontalLabels(new String[]{etiqueta1, etiqueta2, etiqueta3});
            graphHum.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        }


        /*if (graphHum.getSeries().size() == 0) {
            double humMin = Double.valueOf(colocacion.getHumMin());
            double humMax = Double.valueOf(colocacion.getHumMax());
            double humProm = Double.valueOf(colocacion.getHumProm());

            BarGraphSeries<DataPoint> seriesHum = new BarGraphSeries<>(new DataPoint[]{
                    new DataPoint(1, humMin),
                    new DataPoint(2, humMax),
                    new DataPoint(3, humProm),
            });

            seriesHum.setSpacing(50);
            seriesHum.setAnimated(true);
            graphHum.addSeries(seriesHum);

            graphHum.getViewport().setXAxisBoundsManual(true);
            graphHum.getViewport().setYAxisBoundsManual(true);
            graphHum.getViewport().setMinX(0d);
            graphHum.getViewport().setMaxX(4d);
            graphHum.getViewport().setMinY(humMin - 1);
            graphHum.getViewport().setMaxY(humMax + 1);

            //seriesHum.setTitle("Humedad");
            //graphHum.getLegendRenderer().setVisible(true);
            //graphHum.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

            seriesHum.setDrawValuesOnTop(true);
            seriesHum.setValuesOnTopColor(Color.BLACK);

            StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graphHum);
            staticLabelsFormatter.setDynamicLabelFormatter(new DefaultLabelFormatter() {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (!isValueX)
                        return super.formatLabel(value, isValueX) + " %";
                    else
                        return super.formatLabel(value, isValueX);
                }
            });
            staticLabelsFormatter.setHorizontalLabels(new String[]{"               Mín", "Máx", "Promedio       "});
            graphHum.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        }*/
    }

    private void cargarPeriodo(int periodo) {
        BDInterface bd = BDCliente.getClient().create(BDInterface.class);
        Call<RespuestaColocaciones> call = bd.obtenerColocacionesGrafica(periodo);
        call.enqueue(new Callback<RespuestaColocaciones>() {
            @Override
            public void onResponse(Call<RespuestaColocaciones> call, Response<RespuestaColocaciones> response) {
                if (response.body() != null) {
                    if (response.body().getCodigo().equals("1")) {
                        colocaciones = response.body().getColocaciones();
                        prepararGraficaTemp();
                        prepararGraficaHum();
                    } else {
                        colocaciones = null;
                        Toast.makeText(ColocacionGrafica.this, response.body().getMensaje(), Toast.LENGTH_SHORT).show();
                        //llProgressBarLista.setVisibility(View.GONE);
                    }
                } else {
                    colocaciones = null;
                    Toast.makeText(ColocacionGrafica.this, R.string.error_interno_servidor, Toast.LENGTH_SHORT).show();
                    //llProgressBarLista.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<RespuestaColocaciones> call, Throwable t) {
                colocaciones = null;
                Toast.makeText(ColocacionGrafica.this, R.string.error_conexion_servidor, Toast.LENGTH_SHORT).show();
                //llProgressBarLista.setVisibility(View.GONE);
            }

        });
    }
}
