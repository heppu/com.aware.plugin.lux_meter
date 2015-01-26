package com.aware.plugin.lux_meter;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.aware.Aware;
import com.aware.Light;
import com.aware.utils.IContextCard;
import com.aware.providers.Light_Provider.Light_Data;
import com.aware.plugin.lux_meter.Provider.LuxMeter_Data;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ContextCard implements IContextCard {

    private LinearLayout lux_plot;

    private boolean mode;

    private boolean isRegistered = false;

    private static ContextReceiver lightReceiver = new ContextReceiver();

    private static List<Double> luxList = new ArrayList<Double>();

    private static double current = 0;
    private static double avg = 0;
    private static double counter = 0;
    private static boolean lock = false;

    // Made with Receiver because aware reads light data in buffer and makes batch insert, so data won't be in provider in real time
    public static class ContextReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!lock) {
                ContentValues values = (ContentValues) intent.getExtras().get(Light.EXTRA_DATA);
                if(values.get(Light_Data.LIGHT_LUX)!=null) {
                    current = Double.parseDouble(values.get(Light_Data.LIGHT_LUX).toString());
                    avg = avg + current;
                    counter = counter + 1;
                }
            }
        }
    }

    public ContextCard(){};

	String[] x_hours = new String[]{"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23"};
	
	public View getContextCard( Context context ) {
        if(!isRegistered) {
            isRegistered = true;
            try {
                context.unregisterReceiver(lightReceiver);
                IntentFilter contextFilter = new IntentFilter();
                contextFilter.addAction(Light.ACTION_AWARE_LIGHT);
                context.registerReceiver(lightReceiver, contextFilter);
            } catch (IllegalArgumentException e) {
                IntentFilter contextFilter = new IntentFilter();
                contextFilter.addAction(Light.ACTION_AWARE_LIGHT);
                context.registerReceiver(lightReceiver, contextFilter);
            }
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View card = inflater.inflate(R.layout.card, null);

        mode =  Boolean.parseBoolean(Aware.getSetting(context, Settings.MODE_PLUGIN_LUX_METER));

        lux_plot = (LinearLayout) card.findViewById(R.id.lux_plot);
        lux_plot.removeAllViews();
        lux_plot.addView(drawGraph(context));

        return card;
    }

    private GraphicalView drawGraph( Context context ) {
        GraphicalView mChart = null;

        if(mode) {
            lock = true;
            XYSeries frequency_series = new XYSeries("Light (Lux)");

            if(counter == 0) {
                avg = 0;
            } else {
                avg = avg / counter;
            }
            luxList.add(avg);

            //keeps the list size in 60 ~ 1min
            int j = 0;
            for(int i=0; i<luxList.size(); i++) {
                if (luxList.size() - i < 61){
                    frequency_series.add(i-j, luxList.get(i));
                } else {
                    luxList.remove(i);
                    j++;
                }
            }

            counter = 0;
            avg = 0;
            lock = false;

            XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
            dataset.addSeries(frequency_series);

            //setup frequency
            XYSeriesRenderer frequency_renderer = new XYSeriesRenderer();
            frequency_renderer.setColor(Color.BLUE);
            frequency_renderer.setPointStyle(PointStyle.POINT);
            frequency_renderer.setDisplayChartValues(false);
            frequency_renderer.setLineWidth(1);
            frequency_renderer.setFillPoints(true);

            //Setup graph
            XYMultipleSeriesRenderer dataset_renderer = new XYMultipleSeriesRenderer();
            dataset_renderer.setChartTitle("Realtime Lux Chart");
            dataset_renderer.setApplyBackgroundColor(true);
            dataset_renderer.setBackgroundColor(Color.WHITE);
            dataset_renderer.setMarginsColor(Color.WHITE);
            dataset_renderer.setAxesColor(Color.BLACK);
            dataset_renderer.setYTitle("Lux");
            dataset_renderer.setXTitle("Time");
            dataset_renderer.setFitLegend(true);
            dataset_renderer.setXLabels(0);
            dataset_renderer.setYLabels(0);
            dataset_renderer.setPanEnabled(false);
            dataset_renderer.setClickEnabled(false);
            dataset_renderer.setShowAxes(true);
            dataset_renderer.setShowGrid(true);
            dataset_renderer.setShowLabels(true);
            dataset_renderer.setAntialiasing(true);

            //add plot renderers to main renderer
            dataset_renderer.addSeriesRenderer(frequency_renderer);

            //put everything together
            mChart = (GraphicalView) ChartFactory.getLineChartView(context, dataset, dataset_renderer);
        } else {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(System.currentTimeMillis());
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);

            //stores screen on counts grouped per hour
            int[] frequencies = new int[24];

            //add frequencies to the right hour buffer
            Cursor lux_cursor = context.getContentResolver().query(LuxMeter_Data.CONTENT_URI, new String[]{ "avg("+LuxMeter_Data.LUX_AVG+") as luxAvg","strftime('%H',"+ LuxMeter_Data.TIMESTAMP + "/1000, 'unixepoch', 'localtime')+0 as time_of_day" }, LuxMeter_Data.LUX_AVG + " > 0 AND " + LuxMeter_Data.TIMESTAMP + " >= " + c.getTimeInMillis() + " ) GROUP BY ( time_of_day ", null, LuxMeter_Data.TIMESTAMP + " ASC");

            if( lux_cursor != null && lux_cursor.moveToFirst() ) {
                do{
                    frequencies[lux_cursor.getInt(1)] = lux_cursor.getInt(0);

                } while( lux_cursor.moveToNext() );

                lux_cursor.close();
            }

            XYSeries xy_series = new XYSeries("Hour average light (lx)");
            for( int i = 0; i<frequencies.length; i++ ) {
                xy_series.add(i, frequencies[i]);
            }

            XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
            dataset.addSeries(xy_series);

            //Setup the line colors, labels, etc
            XYSeriesRenderer series_renderer = new XYSeriesRenderer();
            series_renderer.setColor(Color.BLACK);
            series_renderer.setPointStyle(PointStyle.POINT);
            series_renderer.setDisplayChartValues(false);
            series_renderer.setLineWidth(2);
            series_renderer.setFillPoints(false);

            //Setup graph colors, labels, etc
            XYMultipleSeriesRenderer dataset_renderer = new XYMultipleSeriesRenderer();
            dataset_renderer.setLabelsColor(Color.BLACK);
            dataset_renderer.setDisplayValues(true);
            dataset_renderer.setFitLegend(false);
            dataset_renderer.setXLabelsColor(Color.BLACK);
            dataset_renderer.setYLabelsColor(0, Color.BLACK);
            dataset_renderer.setLegendHeight(0);
            dataset_renderer.setYLabels(4);
            dataset_renderer.setYTitle("Lux");
            dataset_renderer.setZoomButtonsVisible(false);
            dataset_renderer.setXLabels(0);
            dataset_renderer.setPanEnabled(false);
            dataset_renderer.setShowGridY(false);
            dataset_renderer.setClickEnabled(false);
            dataset_renderer.setAntialiasing(true);
            dataset_renderer.setAxesColor(Color.BLACK);
            dataset_renderer.setApplyBackgroundColor(true);
            dataset_renderer.setBackgroundColor(Color.WHITE);
            dataset_renderer.setMarginsColor(Color.WHITE);
            dataset_renderer.setExternalZoomEnabled(false);
            dataset_renderer.setZoomEnabled(false);

            for(int i=0; i< x_hours.length; i++) {
                dataset_renderer.addXTextLabel(i, x_hours[i]);
            }

            //Add the series renderer to the chart renderer
            dataset_renderer.addSeriesRenderer(series_renderer);

            //Create the chart with our data and setup
            mChart = (GraphicalView) ChartFactory.getBarChartView(context, dataset, dataset_renderer, Type.DEFAULT); //bar chart
        }

        return mChart;
    }

}
