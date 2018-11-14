package com.nugsky.tugasakhir;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RefineryUtilities;

import java.awt.event.WindowEvent;
import java.util.List;

public class CobaChart extends ApplicationFrame {
    /**
     * Constructs a new application frame.
     *
     * @param title the frame title.
     */
    public CobaChart(String title) {
        super(title);
        JFreeChart lineChart = ChartFactory.createLineChart(
                title,
                "Years","Number of Schools",
                createDataset(),
                PlotOrientation.VERTICAL,
                true,true,false);

        ChartPanel chartPanel = new ChartPanel( lineChart );
        chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
        setContentPane( chartPanel );
    }

    private CategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        dataset.addValue( 15 , "schools" , "1970" );
        dataset.addValue( 30 , "schools" , "1980" );
        dataset.addValue( 60 , "schools" ,  "1990" );
        dataset.addValue( 120 , "schools" , "2000" );
        dataset.addValue( 240 , "schools" , "2010" );
        dataset.addValue( 300 , "schools" , "2014" );
        return dataset;
    }

    public static void main( String[ ] args ) {
        CobaChart chart = new CobaChart(
                "School Vs Years" );

        chart.pack( );
        RefineryUtilities.centerFrameOnScreen( chart );
        chart.setVisible( true );
        CobaChart chart2 = new CobaChart(
                "School Vs Years" );

        chart2.pack( );
        RefineryUtilities.centerFrameOnScreen( chart2 );
        chart2.setVisible( true );
    }

    @Override
    public void windowClosing(WindowEvent event) {
        if (event.getWindow() == this) {
            dispose();
        }
    }
}
