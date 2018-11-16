package com.trampas.trampas;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.trampas.trampas.Clases.Colocacion;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ColocacionGrafica extends AppCompatActivity {
    Colocacion colocacion;

    @BindView(R.id.graphHum)
    GraphView graphHum;

    @BindView(R.id.graphTemp)
    GraphView graphTemp;

    @BindView(R.id.tvNombre)
    TextView tvNombre;

    public void setColocacion(Colocacion colocacion) {
        this.colocacion = colocacion;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colocacion_grafica);

        colocacion = (Colocacion) getIntent().getSerializableExtra("colocacion");
        ButterKnife.bind(this);
        getSupportActionBar().setTitle("Gráfica");
        tvNombre.setText(colocacion.getTrampa().getNombre());


        //Graficas
        prepararGraficaTemp();
        prepararGraficaHum();


    }

    public void prepararGraficaTemp() {
        if (graphTemp.getSeries().size() == 0) {
            double tempMin = Double.valueOf(colocacion.getTempMin());
            double tempMax = Double.valueOf(colocacion.getTempMax());
            double tempProm = Double.valueOf(colocacion.getTempProm());

            BarGraphSeries<DataPoint> seriesTemp = new BarGraphSeries<>(new DataPoint[]{
                    new DataPoint(1, tempMin),
                    new DataPoint(2, tempMax),
                    new DataPoint(3, tempProm),
            });

            //Colocar espacio entre barras
            seriesTemp.setSpacing(50);
            seriesTemp.setAnimated(true);
            graphTemp.addSeries(seriesTemp);

            //Modificar tamaño de grafica
            graphTemp.getViewport().setXAxisBoundsManual(true);
            graphTemp.getViewport().setYAxisBoundsManual(true);
            graphTemp.getViewport().setMinX(0d);
            graphTemp.getViewport().setMaxX(4d);
            graphTemp.getViewport().setMinY(tempMin - 1);
            graphTemp.getViewport().setMaxY(tempMax + 1);

            //Colocar leyenda
            //seriesTemp.setTitle("Temperatura");
            //graphTemp.getLegendRenderer().setVisible(true);
            //graphTemp.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

            //Colocar valores encima de la barra
            seriesTemp.setDrawValuesOnTop(true);
            seriesTemp.setValuesOnTopColor(Color.BLACK);

            seriesTemp.setValueDependentColor(new ValueDependentColor<DataPoint>() {
                @Override
                public int get(DataPoint data) {
                    return Color.RED;
                }
            });

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
            staticLabelsFormatter.setHorizontalLabels(new String[]{"              Mín", "Máx", "Promedio       "});
            graphTemp.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        }
    }

    public void prepararGraficaHum() {
        if (graphHum.getSeries().size() == 0) {
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
        }
    }
}
