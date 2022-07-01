
/*
 * TWOWLS.ORG PROPRIETARY/CONFIDENTIAL
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.twowls.lab.legacy.linde.gui;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import org.twowls.lab.legacy.linde.producer.Producer;
import org.twowls.lab.legacy.linde.producer.ProducerException;
import org.twowls.lab.legacy.linde.document.LindenmayerDocument;
import org.twowls.lab.legacy.linde.render.AngleDeviator;
import org.twowls.lab.legacy.linde.render.PlanarTurtleRenderer;
import org.twowls.lab.legacy.linde.render.Surface;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 *
 * @author da
 */
public class LindenmayerFrame extends javax.swing.JFrame {

	private static final long serialVersionUID = 1L;

	private LindenmayerDocument document = null;
    private BufferedImageSurface renderSurface = null;
    private BufferedImage fractalImage = null;
    private Graphics fractalGraphics = null;

    private final Object surfaceLock = new Object();
    private Timer renderTimer = null;
    private boolean isRendering = false;
    private File lastOpenDir = null;

    /** Creates new form LindenmayerFrame */
    public LindenmayerFrame() {
        initComponents();
        renderSurface = new BufferedImageSurface();

        // Create default document
        doNew();
    }

    private void initDocument() {
        rulesText.setText(document.getRules());
        axiomField.setText(document.getAxiom());
        initialAngleSpinner.setValue(document.getInitialAngle());
        turnAngleSpinner.setValue(document.getTurnAngle());
        growFactorSpinner.setValue(document.getGrowFactor());
        orderSpinner.setValue(document.getOrder());

        if (fractalGraphics != null) {
            fractalGraphics.dispose();
            fractalGraphics = null;
        }

        fractalImage = null;

        renderSurface.setPreferredSize(document.getDocumentSize());
        renderSurface.setMinimumSize(document.getDocumentSize());
        renderScroller.setViewportView(renderSurface);
    }

    private void initFractalImage(int iw, int ih) {
        fractalImage = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_RGB);

        fractalGraphics = fractalImage.createGraphics();

        fractalGraphics.setColor(document.getBackground());
        fractalGraphics.fillRect(0, 0, fractalImage.getWidth(),
                fractalImage.getHeight());
    }

    private void parseRule(Producer producer, String rule) throws Exception {
        int expected = 0;
        char symbol = (char)0, mean = (char)0;
        String expression = null;

        for (int i = 0; i < rule.length(); i++) {
            char c = rule.charAt(i);
            if (Character.isWhitespace(c)) {
                continue;
            }

            switch (expected) {
                case 0: // expected rule symbol
                    if (Producer.isInvalidSymbol(c)) {
                        throw new Exception("Invalid symbol: " + c);
                    }
                    symbol = c;
                    expected = 1;
                    break;

                case 1: // expected = or (
                    if (c == '=') {
                        expected = 5;
                    }
                    else if (c == '(') {
                        expected = 2;
                    }
                    else {
                        throw new Exception("Expected '=' or '('");
                    }
                    break;

                case 2: // expected mean
                    if (Producer.isInvalidSymbol(c)) {
                        throw new Exception("Invalid mean: " + c);
                    }
                    mean = c;
                    expected = 3;
                    break;

                case 3: // expected )
                    if (c == ')') {
                        expected = 4;
                    }
                    else {
                        throw new Exception("Expected ')'");
                    }
                    break;

                case 4: // expected =
                    if (c == '=') {
                        expected = 5;
                    }
                    else {
                        throw new Exception("Expected '='");
                    }
                    break;

                case 5: // expression
                    expression = rule.substring(i);
                    break;

                default:
                    throw new Exception("Internal parse error.");
            }

            if (expression != null) {
                producer.addRule(symbol, expression, mean);
                break;
            }
        }
    }

    @Action
    public void doNew() {
        document = LindenmayerDocument.create();
        initDocument();
    }

    @Action
    public void doOpen() {
        JFileChooser jfc = new JFileChooser(lastOpenDir == null
                ? new File(".") : lastOpenDir);

        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "L-System Files (*.lsxml, *.xml)", "lsxml", "xml");

        jfc.setFileFilter(filter);
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                lastOpenDir = jfc.getCurrentDirectory();
                document = LindenmayerDocument.createFromFile(jfc.getSelectedFile());
                initDocument();
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }

    @Action
    public void setup() {
        SetupDialog dlg = new SetupDialog(this, true);
        dlg.setVisible(true);

        if (dlg.getReturnStatus() == SetupDialog.RET_OK) {

        }
    }

    @Action
    public Task<Object, String> doRender() {
        // Initialize producer
        Producer producer = new Producer(true);
        PlanarTurtleRenderer renderer;

        try {
            renderer = new PlanarTurtleRenderer(
                    (Double) initialAngleSpinner.getValue(),
                    (Double) turnAngleSpinner.getValue(),
                    (Double) growFactorSpinner.getValue());

            List<Object> deviators = document.getDeviators();
            if (deviators != null) {
                deviators.stream().filter(dev -> dev != null && dev instanceof AngleDeviator)
                        .forEach(dev -> renderer.addAngleDeviator((AngleDeviator) dev));
            }

            producer.addInterpreter(renderer);

            for (String line : rulesText.getText().split("\n")) {
                parseRule(producer, line);
            }

            producer.setAxiom(axiomField.getText());
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            return null;
        }

        if (renderTimer == null) {
            renderTimer = new Timer(100, e -> {
                if (isRendering) {
                    renderSurface.repaint();
                }
            });
        }

        renderTimer.start();

        return new RenderTask(Application.getInstance(), producer, renderer,
                (Integer) orderSpinner.getValue());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        documentPane = new javax.swing.JPanel();
        openButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        rulesText = new NoTabTextArea();
        rulesLabel = new javax.swing.JLabel();
        axiomLabel = new javax.swing.JLabel();
        axiomField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        initialAngleSpinner = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        turnAngleSpinner = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        growFactorSpinner = new javax.swing.JSpinner();
        newButton = new javax.swing.JButton();
        setupButton = new javax.swing.JButton();
        contentPane = new javax.swing.JPanel();
        renderControl = new javax.swing.JPanel();
        orderSpinner = new javax.swing.JSpinner();
        orderLabel = new javax.swing.JLabel();
        renderButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        statusLabel = new javax.swing.JLabel();
        renderScroller = new javax.swing.JScrollPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setName("mainFrame"); // NOI18N

        jSplitPane1.setName("jSplitPane1"); // NOI18N

        documentPane.setMinimumSize(new java.awt.Dimension(200, 0));
        documentPane.setName("documentPane"); // NOI18N
        documentPane.setPreferredSize(new java.awt.Dimension(250, 393));

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance().getContext().getActionMap(LindenmayerFrame.class, this);
        openButton.setAction(actionMap.get("doOpen")); // NOI18N
        openButton.setText("open");
        openButton.setFocusPainted(false);
        openButton.setIconTextGap(0);
        openButton.setName("openButton"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        rulesText.setColumns(20);
        rulesText.setFont(new java.awt.Font("Monospaced", 0, 12));
        rulesText.setRows(5);
        rulesText.setName("rulesText"); // NOI18N
        jScrollPane1.setViewportView(rulesText);

        rulesLabel.setText("rules");
        rulesLabel.setName("rulesLabel"); // NOI18N

        axiomLabel.setText("axiom");
        axiomLabel.setName("axiomLabel"); // NOI18N

        axiomField.setFont(new java.awt.Font("Monospaced", 0, 12));
        axiomField.setName("axiomField"); // NOI18N

        jLabel1.setText("Initial angle:");
        jLabel1.setName("jLabel1"); // NOI18N

        initialAngleSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 360.0d, 1.0d));
        initialAngleSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(initialAngleSpinner, "0.00"));
        initialAngleSpinner.setName("initialAngleSpinner"); // NOI18N

        jLabel2.setText("Turn angle:");
        jLabel2.setName("jLabel2"); // NOI18N

        turnAngleSpinner.setModel(new javax.swing.SpinnerNumberModel(90.0d, 0.0d, 360.0d, 1.0d));
        turnAngleSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(turnAngleSpinner, "0.00"));
        turnAngleSpinner.setName("turnAngleSpinner"); // NOI18N

        jLabel3.setText("Grow factor:");
        jLabel3.setName("jLabel3"); // NOI18N

        growFactorSpinner.setModel(new javax.swing.SpinnerNumberModel(1.0d, 0.01d, null, 1.0d));
        growFactorSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(growFactorSpinner, "0.00"));
        growFactorSpinner.setName("growFactorSpinner"); // NOI18N

        newButton.setAction(actionMap.get("doNew")); // NOI18N
        newButton.setText("new");
        newButton.setFocusPainted(false);
        newButton.setIconTextGap(0);
        newButton.setName("newButton"); // NOI18N

        setupButton.setAction(actionMap.get("setup")); // NOI18N
        setupButton.setText("setup");
        setupButton.setFocusPainted(false);
        setupButton.setIconTextGap(0);
        setupButton.setName("setupButton"); // NOI18N

        javax.swing.GroupLayout documentPaneLayout = new javax.swing.GroupLayout(documentPane);
        documentPane.setLayout(documentPaneLayout);
        documentPaneLayout.setHorizontalGroup(
            documentPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(documentPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(documentPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(documentPaneLayout.createSequentialGroup()
                        .addComponent(newButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(openButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(setupButton))
                    .addComponent(rulesLabel)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
                    .addComponent(axiomLabel)
                    .addComponent(axiomField, javax.swing.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
                    .addGroup(documentPaneLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 94, Short.MAX_VALUE)
                        .addComponent(initialAngleSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, documentPaneLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 98, Short.MAX_VALUE)
                        .addComponent(turnAngleSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, documentPaneLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 95, Short.MAX_VALUE)
                        .addComponent(growFactorSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        documentPaneLayout.setVerticalGroup(
            documentPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(documentPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(documentPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(newButton)
                    .addComponent(openButton)
                    .addComponent(setupButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rulesLabel)
                .addGap(1, 1, 1)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(axiomLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(axiomField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(documentPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(initialAngleSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(documentPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(turnAngleSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(documentPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(growFactorSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jSplitPane1.setLeftComponent(documentPane);

        contentPane.setName("contentPane"); // NOI18N
        contentPane.setLayout(new java.awt.BorderLayout());

        renderControl.setName("renderControl"); // NOI18N
        renderControl.setPreferredSize(new java.awt.Dimension(342, 40));

        orderSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        orderSpinner.setName("orderSpinner"); // NOI18N

        orderLabel.setText("order:");
        orderLabel.setName("orderLabel"); // NOI18N

        renderButton.setAction(actionMap.get("doRender")); // NOI18N
        renderButton.setFocusPainted(false);
        renderButton.setIconTextGap(0);
        renderButton.setName("renderButton"); // NOI18N

        stopButton.setText("stop");
        stopButton.setFocusPainted(false);
        stopButton.setIconTextGap(0);
        stopButton.setName("stopButton"); // NOI18N

        statusLabel.setText("status");
        statusLabel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        statusLabel.setName("statusLabel"); // NOI18N

        javax.swing.GroupLayout renderControlLayout = new javax.swing.GroupLayout(renderControl);
        renderControl.setLayout(renderControlLayout);
        renderControlLayout.setHorizontalGroup(
            renderControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(renderControlLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(orderLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(orderSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(renderButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(stopButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                .addContainerGap())
        );
        renderControlLayout.setVerticalGroup(
            renderControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(renderControlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(renderControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(statusLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, renderControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(orderSpinner)
                        .addComponent(renderButton, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                        .addComponent(stopButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(orderLabel)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        contentPane.add(renderControl, java.awt.BorderLayout.PAGE_START);

        renderScroller.setName("renderScroller"); // NOI18N
        contentPane.add(renderScroller, java.awt.BorderLayout.CENTER);

        jSplitPane1.setRightComponent(contentPane);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField axiomField;
    private javax.swing.JLabel axiomLabel;
    private javax.swing.JPanel contentPane;
    private javax.swing.JPanel documentPane;
    private javax.swing.JSpinner growFactorSpinner;
    private javax.swing.JSpinner initialAngleSpinner;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JButton newButton;
    private javax.swing.JButton openButton;
    private javax.swing.JLabel orderLabel;
    private javax.swing.JSpinner orderSpinner;
    private javax.swing.JButton renderButton;
    private javax.swing.JPanel renderControl;
    private javax.swing.JScrollPane renderScroller;
    private javax.swing.JLabel rulesLabel;
    private javax.swing.JTextArea rulesText;
    private javax.swing.JButton setupButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JButton stopButton;
    private javax.swing.JSpinner turnAngleSpinner;
    // End of variables declaration//GEN-END:variables

    class BufferedImageSurface extends JPanel implements Surface {

		private static final long serialVersionUID = 1L;

		private double scaleX = 0.0;
        private double scaleY = 0.0;

        private int originX = 0;
        private int originY = 0;

        private boolean flipX = false;
        private boolean flipY = false;

        public void setExtent(double north, double east, double south, double west) {
            double fw = Math.abs(east - west);
            double fh = Math.abs(south - north);

            int dw = document.getWidth();
            int dh = document.getHeight();
            double da = (double) dw / dh;
            int iw = 0, ih = 0;

            if (fw == 0.0 || fh == 0.0) {
                if (fw == 0.0) {
                    iw = 1;
                }

                if (fh == 0.0) {
                    ih = 1;
                }

                if (iw == 0) {
                    iw = dw;
                }

                if (ih == 0) {
                    ih = dh;
                }
            }
            else {
                double fa = fw / fh;

                if (fa < da) {
                    ih = dh;
                    iw = (int) (ih * fa);
                    if (iw < 1) {
                        iw = 1;
                    }
                }
                else {
                    iw = dw;
                    ih = (int) (iw / fa);
                    if (ih < 1) {
                        ih = 1;
                    }
                }
            }

            if (iw > dw || ih > dh) {
                throw new IllegalStateException("wrong image size!!!");
            }

            initFractalImage(iw, ih);

            scaleX = (fw == 0.0) ? 0.0 : (double) (iw - 1) / fw;
            scaleY = (fh == 0.0) ? 0.0 : (double) (ih - 1) / fh;

            originX = (int) (Math.abs(west) * scaleX);
            originY = (int) (Math.abs(north) * scaleY);
        }

        private int transformX(double coord) {
            int x = (int)(coord * scaleX) + originX;
            return flipX ? getWidth() - x : x;
        }

        private int transformY(double coord) {
            int y = (int)(coord * scaleY) + originY;
            return flipY ? getHeight() - y : y;
        }

        public void draw(double srcX, double srcY, double dstX, double dstY) {
            if (srcX != dstX || dstX != dstY) {
                synchronized (surfaceLock) {
                    fractalGraphics.setColor(document.getForeground());
                    fractalGraphics.drawLine(
                            transformX(srcX),
                            transformY(srcY),
                            transformX(dstX),
                            transformY(dstY));
                }
            }
        }

        @Override
        public void paint(Graphics g) {
            g.setColor(document.getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            if (fractalImage != null) {
                synchronized (surfaceLock) {
                    g.drawImage(fractalImage,
                            (getWidth() - fractalImage.getWidth()) /2,
                            (getHeight() - fractalImage.getHeight()) /2,
                            null);
                }
            }
        }
    }

    private class RenderTask extends Task<Object, String> {

        private Producer producer = null;
        private PlanarTurtleRenderer renderer = null;
        private int order = 0;

        RenderTask(Application app, Producer producer,
                PlanarTurtleRenderer renderer, int order) {

            super(app);
            this.producer = producer;
            this.renderer = renderer;
            this.order = order;
        }

        @Override
        protected Object doInBackground() throws ProducerException {
            // Produce & cache fractal
            publish("Buffering fractal...");
            producer.produce(order);

            publish("Rendering fractal (" + renderer.getCacheMoves() + " moves)...");
            isRendering = true;
            try {
                renderer.render(renderSurface);
            }
            finally {
                isRendering = false;
            }

            return null;
        }

        @Override
        protected void process(List<String> list) {
            super.process(list);
            if (list.size() > 0) {
                statusLabel.setText(list.get(0));
            }
        }

        @Override
        protected void failed(Throwable thrwbl) {
            super.failed(thrwbl);
            JOptionPane.showMessageDialog(LindenmayerFrame.this,
                    thrwbl.getMessage());
        }

        @Override
        protected void finished() {
            super.finished();
            renderSurface.repaint();
            renderTimer.stop();

            fractalGraphics.dispose();
            fractalGraphics = null;
        }
    }
}
