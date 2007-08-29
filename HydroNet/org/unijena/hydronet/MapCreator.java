package org.unijena.hydronet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.*;
import javax.swing.SpringLayout;
import java.awt.*;
import javax.swing.SpringLayout;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.*;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYStepAreaRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.xml.transform.TransformerException;
import javax.swing.JDialog;

import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.CoordinateSystemFactory;

import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;

import org.geotools.data.collection.CollectionDataStore;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.shapefile.ShapefileDataStore;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;

import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.filter.BBoxExpression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.GeometryDistanceFilter;
import org.geotools.filter.IllegalFilterException;

import org.geotools.gui.swing.StyledMapPane;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.renderer.j2d.GeoMouseEvent;

import org.geotools.renderer.j2d.RenderedMapScale;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;

import org.unijena.jams.data.JAMSEntity;
import org.unijena.jams.data.JAMSEntityCollection;
import org.unijena.jams.data.JAMSInteger;

import org.unijena.jams.data.JAMSString;
import org.unijena.jams.model.JAMSGUIComponent;
import org.unijena.jams.model.JAMSVarDescription;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import jams.components.gui.*;

/**
 *
 * @author C. Fischer
 */

public class MapCreator extends jams.components.gui.MapCreator {
	
	@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Name of SLD-File containing layer style information"
            )
            public JAMSString stylesFileName;
	
	@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "ID of a style in the SLD-File"
            )
            public JAMSInteger styleID;
	
	@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Data file directory name"
            )
            public JAMSString dirName;
	
	@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Collection of hru objects"
            )
            public JAMSEntityCollection hrus;
	
	@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Name of hru attribute to add for mapping"
            )
            public JAMSString showAttr ;
	
	@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Number of ranges for classification attribute"
            )
            public JAMSInteger numOfRanges;
	
	@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Color shading the ranges"
            )
            public JAMSString rangeColor;
	
	@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Name of shapefile to add as a layer to the map"
            )
            public JAMSString shapeFileName1;
	
	@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Name of shapefile to add as a layer to the map"
            )
            public JAMSString shapeFileName2;
	
	@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Name of shapefile to add as a layer to the map"
            )
            public JAMSString shapeFileName3;
	
	@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Colors for extra shapefiles"
            )
            public JAMSString shapeColors;
	
	@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Original shape file name"
            )
            public JAMSString baseShape;
	
	private JTextArea idfield;		
	private JFreeChart chart;
	private XYPlot plot;
	private XYSeries dataset1;

	private JLabel InterflowWeight;
	private JLabel percolation_weight;
	private JButton downstreamID;
	private JLabel ist_input;
	private JLabel cur_input;
	
	@SuppressWarnings("unchecked")

	public void run() throws Exception {
	    super.hrus = hrus;
	    super.stylesFileName = stylesFileName;
	    super.styleID = styleID;
	    super.dirName = dirName;
//	    super.showAttr = showAttr;
//	    super.numOfRanges = numOfRanges;
//	    super.rangeColor = rangeColor;
	    super.shapeFileName1 = shapeFileName1;
	    super.shapeFileName2 = shapeFileName2;
	    super.shapeFileName3 = shapeFileName3;
//	    super.shapeColors = shapeColors;
	    super.baseShape = baseShape;
	    super.run();
	}
	protected void getFeature(java.awt.geom.Point2D p) throws IOException {
	    super.getFeature(p);
	    
	    try {
		Feature f = this.getSelectedFeature();
		if (f != null) {
		    ShowActFunction(new Double(f.getID()).intValue());
		}
	    }
	    catch (Exception e) {
		this.getContext().getModel().getRuntime().println(e.toString());
	    }
	}
	
				
	
	protected void createPanel() {	    			    
	    //super.createPanel();
	    
	    try {	
		plot = new XYPlot();
		plot.setDomainAxis(new NumberAxis("Nitrogen Input"));
		plot.setRangeAxis(new NumberAxis("Nitrogen Output"));
		chart = new JFreeChart(plot);
        
		dataset1 = new XYSeries("Activation - Function");
                
		plot.setDataset(0, new XYSeriesCollection(dataset1));
		plot.setRenderer(0, new DefaultXYItemRenderer());
	           
		JFrame frame = new JFrame("Activation - Function");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(new ChartPanel(chart), BorderLayout.CENTER);
        	    	    
		JPanel panel = new JPanel(new FlowLayout());
	
		idfield = new JTextArea(1,40);
		idfield.setEditable(true);
		panel.add(idfield, BorderLayout.WEST);
	    
		JButton bShow = new JButton("Show");
		
		
		//information panel				
		JLabel label1 = new JLabel("Interflow - Weight: ");
		JLabel label2 = new JLabel("Percolation - Weight: ");
		JLabel label3 = new JLabel("DownstreamID: ");
		JLabel label4 = new JLabel("Input (old): ");
		JLabel label5 = new JLabel("Input (new): ");
		
		InterflowWeight = new JLabel();
		percolation_weight = new JLabel();
		downstreamID = new JButton("-");
		
		downstreamID.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e)
		    {
			try {
			    ShowActFunction(new Integer(downstreamID.getText()).intValue());
			}
			catch(Exception e2) {
			}
		    }});
		
		ist_input = new JLabel();
		cur_input = new JLabel();
			
		JPanel infopanel = new JPanel(new SpringLayout());
		
		
		infopanel.add(label1);
		infopanel.add(InterflowWeight);
		
		infopanel.add(label2);
		infopanel.add(percolation_weight);
		
		infopanel.add(label3);
		infopanel.add(downstreamID);
		
		infopanel.add(label4);
		infopanel.add(ist_input);
		
		infopanel.add(label5);
		infopanel.add(cur_input);
						
		makeCompactGrid(infopanel,
                                5, 2, //rows, cols
                                6, 6,        //initX, initY
                                6, 6);       //xPad, yPad

		bShow.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e)
		    {
			try {
			    ShowActFunction(new Integer(idfield.getText()).intValue());
			}
			catch(Exception e2) {
			}
		    }});
		panel.add(bShow,BorderLayout.EAST);
	    
		frame.getContentPane().add(panel,BorderLayout.SOUTH);        
		frame.getContentPane().add(infopanel,BorderLayout.EAST);        
		frame.setBounds(50, 50, 800, 500);
		frame.setVisible(true);	    	    
	    } catch (Exception e) {
		this.getContext().getModel().getRuntime().println(e.toString());
	}     
    }
	    
    void ShowActFunction(int id) throws Exception {	    
	JAMSEntity e = null;
	for (int i = 0;i<hrus.getEntities().size();i++) {
	    e = hrus.getEntities().get(i);
	    if (e.getDouble("ID") == id)
		break;
	}
	if (e == null)
	    return;
	NONeuron nitr_neuron = (NONeuron)e.getObject("NITROGEN_NEURON");
			
	GenericFunction f = nitr_neuron.getFilter(0);
	dataset1.clear();
	if ( f.getFunction().getType() ==  ActivationFunction.LINAPPROX) {
	    Matrix M = ((LinApprox)f.getFunction()).getData();
	    for (int i=0;i<M.rows;i++) {
		dataset1.add(M.element[i][0],M.element[i][1]);
	    }			    			    			    
	}
	
	idfield.setText(new Integer(id).toString());

	InterflowWeight.setText((new Double(((NONeuron)e.getObject("NITROGEN_NEURON")).getDownstreamWeight())).toString());
	percolation_weight.setText((new Double(((NONeuron)e.getObject("NITROGEN_NEURON")).getOutputWeight())).toString());
	ist_input.setText((new Double(((DistNeuron)e.getObject("DIST_NEURON")).getInitalExternInput())).toString());
	cur_input.setText((new Double(((DistNeuron)e.getObject("DIST_NEURON")).getInput())).toString());
	downstreamID.setText(new Integer((new Double(e.getDouble("ID"))).intValue()).toString());
    }
    /* Used by makeCompactGrid. */
    private static SpringLayout.Constraints getConstraintsForCell(
                                                int row, int col,
                                                Container parent,
                                                int cols) {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component c = parent.getComponent(row * cols + col);
        return layout.getConstraints(c);
    }

    /**
     * Aligns the first <code>rows</code> * <code>cols</code>
     * components of <code>parent</code> in
     * a grid. Each component in a column is as wide as the maximum
     * preferred width of the components in that column;
     * height is similarly determined for each row.
     * The parent is made just big enough to fit them all.
     *
     * @param rows number of rows
     * @param cols number of columns
     * @param initialX x location to start the grid at
     * @param initialY y location to start the grid at
     * @param xPad x padding between cells
     * @param yPad y padding between cells
     */
    private static void makeCompactGrid(Container parent,
                                       int rows, int cols,
                                       int initialX, int initialY,
                                       int xPad, int yPad) {
        SpringLayout layout;
        try {
            layout = (SpringLayout)parent.getLayout();
        } catch (ClassCastException exc) {
            System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
            return;
        }

        //Align all cells in each column and make them the same width.
        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width,
                                   getConstraintsForCell(r, c, parent, cols).
                                       getWidth());
            }
            for (int r = 0; r < rows; r++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(r, c, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }

        //Align all cells in each row and make them the same height.
        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++) {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++) {
                height = Spring.max(height,
                                    getConstraintsForCell(r, c, parent, cols).
                                        getHeight());
            }
            for (int c = 0; c < cols; c++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(r, c, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }

        //Set the parent's size.
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }
}