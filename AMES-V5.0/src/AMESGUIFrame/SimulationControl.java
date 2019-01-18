/* ============================================================================
 * AMES Wholesale Power Market Test Bed (Java): A Free Open-Source Test-Bed  
 *         for the Agent-based Modeling of Electricity Systems 
 * ============================================================================
 *
 * (C) Copyright 2008, by Hongyan Li, Junjie Sun, and Leigh Tesfatsion
 *
 *    Homepage: http://www.econ.iastate.edu/tesfatsi/AMESMarketHome.htm
 *
 * LICENSING TERMS
 * The AMES Market Package is licensed by the copyright holders (Junjie Sun, 
 * Hongyan Li, and Leigh Tesfatsion) as free open-source software under the       
 * terms of the GNU General Public License (GPL). Anyone who is interested is 
 * allowed to view, modify, and/or improve upon the code used to produce this 
 * package, but any software generated using all or part of this code must be 
 * released as free open-source software in turn. The GNU GPL can be viewed in 
 * its entirety as in the following site: http://www.gnu.org/licenses/gpl.html
 */

/*
 * SimulationControl.java
 *
 * Created on June 14, 2007, 2:32 PM
 */

package AMESGUIFrame;


import javax.swing.JOptionPane;
import java.util.Date;

public class SimulationControl extends javax.swing.JFrame {
    
    /** Creates new form SimulationControl 
     * @param frame 
     * @param bShow 
     */
    public SimulationControl(AMESFrame frame, boolean bShow) {
        amesFrame=frame;
        bPrevNextButtonShown=bShow;
        bOkSelect=false;
        
        initComponents();
        MaximumDayCheckBox.setSelected(bMaximumDay);
        ThresholdCheckBox.setSelected(bThreshold);
        DailyNetEarningThresholdCheckBox.setSelected(bDailyNetEarningThreshold);
        ActionProbabilityCheckBox.setSelected(bActionProbabilityCheck);
        LearningParameterCheckBox.setSelected(bLearningCheck);
        
        if(bPrevNextButtonShown){
            setTitle("Step 6: Simulation control parameters");
            CancelButton.setVisible(false);
            OKButton.setVisible(false);
        }
        else{
            setTitle("Simulation control parameters");
            PrevButton.setVisible(false);
            DoneButton.setVisible(false);
        }
    }
    
    public void SetInitParameters(int iMax, boolean bMax, double dThreshold, boolean bThresh, double dEarningThreshold, boolean bEarningThresh, int iEarningStart, int iEarningLength, int iStart, int iLength, double dCheck, boolean bCheck, int iLearnStart, int iLearnLength, double dLearnCheck, boolean bLearnCheck, double dGCap, double dLseCap, long lRandom, int iPriceSensitiveLSE, String shostName, String sdatabaseName, String suserName, String spassword, int dLseData){
        iMaxDay=iMax;
        bMaximumDay=bMax;
        dThresholdProbability=dThreshold;
        dDailyNetEarningThreshold=dEarningThreshold;
        bDailyNetEarningThreshold=bEarningThresh;
        iDailyNetEarningStartDay=iEarningStart;
        iDailyNetEarningDayLength=iEarningLength;
        iStartDay=iStart;
        iCheckDayLength=iLength;
        dActionProbability=dCheck;
        bActionProbabilityCheck=bCheck;
        iLearningCheckStartDay=iLearnStart;
        iLearningCheckDayLength=iLearnLength;
        dLearningCheckDifference=dLearnCheck;
        bLearningCheck=bLearnCheck;
        bThreshold=bThresh;
        dGenPriceCap=dGCap;
        dLSEPriceCap=dLseCap;
        RandomSeed=lRandom;
        priceSensitiveLSE=iPriceSensitiveLSE;
        databaseName=sdatabaseName;
        userName=suserName;
        hostName=shostName;
        password=spassword;
        iLseData=dLseData;
    
        MaxDayTextField.setText(String.valueOf(iMaxDay));
        ThresholdProbabilityTextField.setText(String.valueOf(dThresholdProbability));
        DailyNetEarningsThresholdTextField.setText(String.valueOf(dDailyNetEarningThreshold));
        DailyNetEarningStartDayTextField.setText(String.valueOf(iDailyNetEarningStartDay));
        DailyNetEarningDayLengthTextField.setText(String.valueOf(iDailyNetEarningDayLength));
        StartDayTextField.setText(String.valueOf(iStartDay));
        CheckDayLengthTextField.setText(String.valueOf(iCheckDayLength));
        ProbabilityDifferenceTextField.setText(String.valueOf(dActionProbability));
        LearningCheckStartDayTextField.setText(String.valueOf(iLearningCheckStartDay));
        LearningCheckDayLengthTextField.setText(String.valueOf(iLearningCheckDayLength));
        LearningCheckDifferenceTextField.setText(String.valueOf(dLearningCheckDifference));
        GenPriceCapTextField.setText(String.valueOf(dGenPriceCap));
        LSEPriceCapTextField.setText(String.valueOf(dLSEPriceCap));
        RandomSeedTextField1.setText(String.valueOf(RandomSeed));
        PriceSensitiveLSETextField1.setText(String.valueOf(priceSensitiveLSE));
        HostNameTextField.setText(hostName);
        DatabaseNameTextField.setText(databaseName);
        UserNameTextField.setText(userName);        
        PasswordTextField.setText(password);
        MaximumDayCheckBox.setSelected(bMaximumDay);
        ThresholdCheckBox.setSelected(bThreshold);
        DailyNetEarningThresholdCheckBox.setSelected(bDailyNetEarningThreshold);
        ActionProbabilityCheckBox.setSelected(bActionProbabilityCheck);
        LearningParameterCheckBox.setSelected(bLearningCheck);
        
   }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        radioButtonGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        MaxDayTextField = new javax.swing.JTextField();
        ThresholdProbabilityTextField = new javax.swing.JTextField();
        MaximumDayCheckBox = new javax.swing.JCheckBox();
        ThresholdCheckBox = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        ActionProbabilityCheckBox = new javax.swing.JCheckBox();
        jLabel9 = new javax.swing.JLabel();
        StartDayTextField = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        CheckDayLengthTextField = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        ProbabilityDifferenceTextField = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        LearningParameterCheckBox = new javax.swing.JCheckBox();
        jLabel12 = new javax.swing.JLabel();
        LearningCheckStartDayTextField = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        LearningCheckDayLengthTextField = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        LearningCheckDifferenceTextField = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        DailyNetEarningThresholdCheckBox = new javax.swing.JCheckBox();
        jLabel15 = new javax.swing.JLabel();
        DailyNetEarningStartDayTextField = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        DailyNetEarningDayLengthTextField = new javax.swing.JTextField();
        DailyNetEarningThresholdTextField = new javax.swing.JLabel();
        DailyNetEarningsThresholdTextField = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        HostNameTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        DatabaseNameTextField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        UserNameTextField = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        PasswordTextField = new javax.swing.JTextField();
        OKButton = new javax.swing.JButton();
        CancelButton = new javax.swing.JButton();
        PrevButton = new javax.swing.JButton();
        DoneButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        GenPriceCapTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        LSEPriceCapTextField = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        RandomSeedTextField1 = new javax.swing.JTextField();
        GenerateButton1 = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        PriceSensitiveLSETextField1 = new javax.swing.JTextField();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Stopping Rule", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 12))); // NOI18N
        jPanel1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        MaxDayTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        MaxDayTextField.setText("3");
        MaxDayTextField.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        MaxDayTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MaxDayTextFieldActionPerformed(evt);
            }
        });

        ThresholdProbabilityTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        ThresholdProbabilityTextField.setText("0.999");
        ThresholdProbabilityTextField.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        MaximumDayCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        MaximumDayCheckBox.setText("Maximum Day");
        MaximumDayCheckBox.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        MaximumDayCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MaximumDayCheckBoxActionPerformed(evt);
            }
        });

        ThresholdCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        ThresholdCheckBox.setText("Threshold Probability");
        ThresholdCheckBox.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        ThresholdCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ThresholdCheckBoxActionPerformed(evt);
            }
        });

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        ActionProbabilityCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        ActionProbabilityCheckBox.setText("Action Probability Check");
        ActionProbabilityCheckBox.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        ActionProbabilityCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ActionProbabilityCheckBoxActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel9.setText("Start Day:");
        jLabel9.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        StartDayTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        StartDayTextField.setText("0");
        StartDayTextField.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        StartDayTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StartDayTextFieldActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel10.setText("Consecutive Day Length:");
        jLabel10.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        CheckDayLengthTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        CheckDayLengthTextField.setText("0");
        CheckDayLengthTextField.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        CheckDayLengthTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CheckDayLengthTextFieldActionPerformed(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel11.setText("Probability Difference:");
        jLabel11.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        ProbabilityDifferenceTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        ProbabilityDifferenceTextField.setText("0");
        ProbabilityDifferenceTextField.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        ProbabilityDifferenceTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProbabilityDifferenceTextFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(ActionProbabilityCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(180, 180, 180))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33)
                        .addComponent(ProbabilityDifferenceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(33, 33, 33)
                            .addComponent(CheckDayLengthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(StartDayTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(ActionProbabilityCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(StartDayTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(CheckDayLengthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(ProbabilityDifferenceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        LearningParameterCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        LearningParameterCheckBox.setText("Action Stability Check");
        LearningParameterCheckBox.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        LearningParameterCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LearningParameterCheckBoxActionPerformed(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel12.setText("Start Day:");
        jLabel12.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        LearningCheckStartDayTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        LearningCheckStartDayTextField.setText("0");
        LearningCheckStartDayTextField.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        LearningCheckStartDayTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LearningCheckStartDayTextFieldActionPerformed(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel13.setText("Consecutive Day Length:");
        jLabel13.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        LearningCheckDayLengthTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        LearningCheckDayLengthTextField.setText("0");
        LearningCheckDayLengthTextField.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        LearningCheckDayLengthTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LearningCheckDayLengthTextFieldActionPerformed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel14.setText("Difference:");
        jLabel14.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        LearningCheckDifferenceTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        LearningCheckDifferenceTextField.setText("0");
        LearningCheckDifferenceTextField.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        LearningCheckDifferenceTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LearningCheckDifferenceTextFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(LearningParameterCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(180, 180, 180))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33)
                        .addComponent(LearningCheckDifferenceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(33, 33, 33)
                            .addComponent(LearningCheckDayLengthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(LearningCheckStartDayTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(LearningParameterCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(LearningCheckStartDayTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(LearningCheckDayLengthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(LearningCheckDifferenceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        DailyNetEarningThresholdCheckBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        DailyNetEarningThresholdCheckBox.setText("Daily Net Earnings Threshold");
        DailyNetEarningThresholdCheckBox.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        DailyNetEarningThresholdCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DailyNetEarningThresholdCheckBoxActionPerformed(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel15.setText("Start Day:");
        jLabel15.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        DailyNetEarningStartDayTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        DailyNetEarningStartDayTextField.setText("1");
        DailyNetEarningStartDayTextField.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        DailyNetEarningStartDayTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DailyNetEarningStartDayTextFieldActionPerformed(evt);
            }
        });

        jLabel16.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel16.setText("Consecutive Day Length:");
        jLabel16.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        DailyNetEarningDayLengthTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        DailyNetEarningDayLengthTextField.setText("0");
        DailyNetEarningDayLengthTextField.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        DailyNetEarningDayLengthTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DailyNetEarningDayLengthTextFieldActionPerformed(evt);
            }
        });

        DailyNetEarningThresholdTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        DailyNetEarningThresholdTextField.setText("Threshold:");
        DailyNetEarningThresholdTextField.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        DailyNetEarningsThresholdTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        DailyNetEarningsThresholdTextField.setText("10.0");
        DailyNetEarningsThresholdTextField.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        DailyNetEarningsThresholdTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DailyNetEarningsThresholdTextFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(DailyNetEarningThresholdCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(180, 180, 180))
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(DailyNetEarningThresholdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33)
                        .addComponent(DailyNetEarningsThresholdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel6Layout.createSequentialGroup()
                            .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(33, 33, 33)
                            .addComponent(DailyNetEarningDayLengthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel6Layout.createSequentialGroup()
                            .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(DailyNetEarningStartDayTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(DailyNetEarningThresholdCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(DailyNetEarningStartDayTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(DailyNetEarningDayLengthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(DailyNetEarningThresholdTextField)
                    .addComponent(DailyNetEarningsThresholdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(MaximumDayCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                                .addGap(31, 31, 31))
                            .addComponent(ThresholdCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(MaxDayTextField)
                            .addComponent(ThresholdProbabilityTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(49, 49, 49))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.LEADING, 0, 319, Short.MAX_VALUE)
                            .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, 0, 319, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(MaximumDayCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ThresholdCheckBox))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(MaxDayTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ThresholdProbabilityTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "SQL Parameters", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 12))); // NOI18N

        jLabel1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel1.setText("Host Name:");
        jLabel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        HostNameTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        HostNameTextField.setText("CO1132-07.ece.iastate.edu");
        HostNameTextField.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        HostNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HostNameTextFieldActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel6.setText("Database Name:");
        jLabel6.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        DatabaseNameTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        DatabaseNameTextField.setText("IRW");
        DatabaseNameTextField.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        DatabaseNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DatabaseNameTextFieldActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel7.setText("User Name:");
        jLabel7.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        UserNameTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        UserNameTextField.setText("root");
        UserNameTextField.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        UserNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UserNameTextFieldActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel8.setText("Password:");
        jLabel8.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        PasswordTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        PasswordTextField.setText("irwproject");
        PasswordTextField.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        PasswordTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PasswordTextFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(DatabaseNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(HostNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(UserNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(PasswordTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(HostNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(DatabaseNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(UserNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(PasswordTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        OKButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        OKButton.setText("Ok");
        OKButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        OKButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OKButtonActionPerformed(evt);
            }
        });

        CancelButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        CancelButton.setText("Cancel");
        CancelButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        CancelButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        CancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CancelButtonActionPerformed(evt);
            }
        });

        PrevButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        PrevButton.setText("<< Prev");
        PrevButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        PrevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PrevButtonActionPerformed(evt);
            }
        });

        DoneButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        DoneButton.setText("Done");
        DoneButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        DoneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DoneButtonActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Price Cap", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 12))); // NOI18N

        GenPriceCapTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        GenPriceCapTextField.setText("1000");
        GenPriceCapTextField.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel2.setText("Max Reported Price For GenCos:");
        jLabel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel3.setText("Min Reported Price For LSEs:");
        jLabel3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        LSEPriceCapTextField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        LSEPriceCapTextField.setText("0");
        LSEPriceCapTextField.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        LSEPriceCapTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LSEPriceCapTextFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(28, 28, 28)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(GenPriceCapTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(LSEPriceCapTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(GenPriceCapTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LSEPriceCapTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        LSEPriceCapTextField.getAccessibleContext().setAccessibleName("");

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Random Seed", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 12))); // NOI18N

        jLabel4.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel4.setText("Random Seed:");
        jLabel4.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        RandomSeedTextField1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        RandomSeedTextField1.setText("695672061");
        RandomSeedTextField1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        RandomSeedTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RandomSeedTextField1ActionPerformed(evt);
            }
        });

        GenerateButton1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        GenerateButton1.setText("Generate");
        GenerateButton1.setToolTipText("Generate a random seed using current time");
        GenerateButton1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        GenerateButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GenerateButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(GenerateButton1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(RandomSeedTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(86, 86, 86))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(RandomSeedTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(GenerateButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Price Sensitive LSE:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 12))); // NOI18N

        jLabel5.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel5.setText("Price Sensitive LSE:");
        jLabel5.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        PriceSensitiveLSETextField1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        PriceSensitiveLSETextField1.setText("3");
        PriceSensitiveLSETextField1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        PriceSensitiveLSETextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PriceSensitiveLSETextField1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(PriceSensitiveLSETextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(86, 86, 86))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(PriceSensitiveLSETextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addComponent(OKButton, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(CancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(PrevButton, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)
                        .addComponent(DoneButton, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 361, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE))))
                .addContainerGap(29, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(22, 22, 22)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(DoneButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(PrevButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(CancelButton)
                            .addComponent(OKButton))
                        .addGap(9, 9, 9)))
                .addContainerGap())
        );

        jPanel2.getAccessibleContext().setAccessibleDescription("");
        jPanel7.getAccessibleContext().setAccessibleName("Price Sensitive LSe");

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void DoneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DoneButtonActionPerformed
    this.setVisible(false);

    // Verify Data
    String strError=amesFrame.checkCaseData();
    if(!strError.isEmpty()){
        JOptionPane.showMessageDialog(this, strError, "Case Data Verify Message", JOptionPane.ERROR_MESSAGE); 
        return;
    }

    amesFrame.setbLoadCase(true);
    amesFrame.setbCaseResult(false);
    amesFrame.enableCaseMenuAndToolBar();
    amesFrame.enableCommandMenuAndToolbar();
    amesFrame.enableOptionsMenu();
    amesFrame.disableViewMenu();
    amesFrame.InitializeAMESMarket();
}//GEN-LAST:event_DoneButtonActionPerformed

private void PrevButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PrevButtonActionPerformed
    this.setVisible(false);
    amesFrame.activeConfig5();
//GEN-LAST:event_PrevButtonActionPerformed
}                                          

private void CancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelButtonActionPerformed
    setVisible(false);
}//GEN-LAST:event_CancelButtonActionPerformed

private void OKButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OKButtonActionPerformed
    String strError=DataVerify();
    if(!strError.isEmpty()){
        JOptionPane.showMessageDialog(this, strError, "Case Data Verify Message", JOptionPane.ERROR_MESSAGE); 
        return;
    }
    
    amesFrame.simulationControl.SetInitParameters(iMaxDay, bMaximumDay, dThresholdProbability, bThreshold, dDailyNetEarningThreshold, bDailyNetEarningThreshold, iDailyNetEarningStartDay, iDailyNetEarningDayLength, iStartDay, iCheckDayLength, dActionProbability, bActionProbabilityCheck, //
                iLearningCheckStartDay, iLearningCheckDayLength, dLearningCheckDifference, bLearningCheck, dGenPriceCap, dLSEPriceCap, RandomSeed, priceSensitiveLSE, hostName, databaseName, userName, password, iLseData);
    
    strError=amesFrame.checkCaseData();
    if(!strError.isEmpty()){
        JOptionPane.showMessageDialog(this, strError, "Case Data Verify Message", JOptionPane.ERROR_MESSAGE); 
        return;
    }

    amesFrame.setbLoadCase(true);
    amesFrame.setbCaseResult(false);
    amesFrame.enableCaseMenuAndToolBar();
    amesFrame.enableCommandMenuAndToolbar();
    amesFrame.enableOptionsMenu();
    amesFrame.disableViewMenu();
    amesFrame.InitializeAMESMarket();
        setVisible(false);
    }//GEN-LAST:event_OKButtonActionPerformed

public String DataVerify(){
    String strTemp=MaxDayTextField.getText();
    int iTemp;
    double dTemp;
    
    String message="The Maximum Day paramter should bigger than 0!";
    try {
       iTemp=Integer.parseInt(strTemp);

        if(iTemp<0) {
             JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
            return message;
        } else
            iMaxDay=iTemp;
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
        return message;
    }
    
    strTemp=ThresholdProbabilityTextField.getText();
    message="The Threshold Probability paramter should between 0 and 1.0!";
    try {
       dTemp=Double.parseDouble(strTemp);

        if((dTemp<0)||(dTemp>1.0)) {
            JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
            return message;
        } else
            dThresholdProbability=dTemp;
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
        return message;
    }
    
    strTemp=DailyNetEarningStartDayTextField.getText();
    message="The Daily Net Earning Start Day paramter should bigger than 0!";
    try {
       iTemp=Integer.parseInt(strTemp);

        if(iTemp<0) {
             JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
            return message;
        } else
            iDailyNetEarningStartDay=iTemp;
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
        return message;
    }
    
    strTemp=DailyNetEarningDayLengthTextField.getText();
    message="The Daily Net Earning Consecutive Day Length paramter should bigger than 0!";
    try {
       iTemp=Integer.parseInt(strTemp);

        if(iTemp<0) {
             JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
            return message;
        } else
            iDailyNetEarningDayLength=iTemp;
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
        return message;
    }
    
    strTemp=DailyNetEarningsThresholdTextField.getText();
    message="The Daily Net Earning Threshold paramter should bigger than 0.0!";
    try {
       dTemp=Double.parseDouble(strTemp);

        if(dTemp<0) {
            JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
            return message;
        } else
            dDailyNetEarningThreshold=dTemp;
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
        return message;
    }
    
    strTemp=StartDayTextField.getText();
    message="The Start Day paramter should bigger than 0!";
    try {
       iTemp=Integer.parseInt(strTemp);

        if(iTemp<0) {
             JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
            return message;
        } else
            iStartDay=iTemp;
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
        return message;
    }
    
    strTemp=CheckDayLengthTextField.getText();
    message="The Consecutive Day Length paramter should bigger than 0!";
    try {
       iTemp=Integer.parseInt(strTemp);

        if(iTemp<0) {
             JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
            return message;
        } else
            iCheckDayLength=iTemp;
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
        return message;
    }
    
    strTemp=ProbabilityDifferenceTextField.getText();
    message="The Probability Difference paramter should bigger than 0!";
    try {
       dTemp=Double.parseDouble(strTemp);

        if(dTemp<0) {
            JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
            return message;
        } else
            dActionProbability=dTemp;
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
        return message;
    }
    
    strTemp=LearningCheckStartDayTextField.getText();
    message="The Start Day paramter should bigger than 0!";
    try {
       iTemp=Integer.parseInt(strTemp);

        if(iTemp<0) {
             JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
            return message;
        } else
            iLearningCheckStartDay=iTemp;
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
        return message;
    }
    
    strTemp=LearningCheckDayLengthTextField.getText();
    message="The Consecutive Day Length paramter should bigger than 0!";
    try {
       iTemp=Integer.parseInt(strTemp);

        if(iTemp<0) {
             JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
            return message;
        } else
            iLearningCheckDayLength=iTemp;
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
        return message;
    }
    
    strTemp=LearningCheckDifferenceTextField.getText();
    message="The Probability Difference paramter should bigger than 0!";
    try {
       dTemp=Double.parseDouble(strTemp);

        if(dTemp<0) {
            JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
            return message;
        } else
            dLearningCheckDifference=dTemp;
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
        return message;
    }
    
    strTemp=GenPriceCapTextField.getText();
    message="The GenCos PriceCap paramter should bigger than 0!";
    try {
       dTemp=Double.parseDouble(strTemp);

        if(dTemp<0) {
            JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
            return message;
        } else
            dGenPriceCap=dTemp;
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
        return message;
    }
    
    strTemp=LSEPriceCapTextField.getText();
    message="The LSE PriceCap paramter should bigger than 0!";
    try {
       dTemp=Double.parseDouble(strTemp);

        if(dTemp<0) {
            JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
            return message;
        } else
            dLSEPriceCap=dTemp;
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
        return message;
    }
    
    message="The random seed is error!";
    strTemp=RandomSeedTextField1.getText();
    long lTemp;
    try {
        lTemp=Long.parseLong(strTemp);

        RandomSeed=lTemp;
    }
    catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE); 
        return message;
      }
    
    message="Price sensitive LSE should be less than the number of LSEs";
    strTemp=PriceSensitiveLSETextField1.getText();
    try {
        
        iTemp=Integer.parseInt(strTemp);
        if(iTemp>iLseData) {
            JOptionPane.showMessageDialog(this, message, "Error Message", JOptionPane.ERROR_MESSAGE);
            return message;
        }
        

        priceSensitiveLSE=iTemp;
    }
    catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE); 
        return message;
      }
    
    amesFrame.SetSimulationParameters(iMaxDay, bMaximumDay, dThresholdProbability, bThreshold, dDailyNetEarningThreshold, bDailyNetEarningThreshold, iDailyNetEarningStartDay, iDailyNetEarningDayLength,
                iStartDay, iCheckDayLength, dActionProbability, bActionProbabilityCheck, iLearningCheckStartDay, iLearningCheckDayLength, dLearningCheckDifference, bLearningCheck, dGenPriceCap, dLSEPriceCap, RandomSeed, priceSensitiveLSE, hostName, databaseName, userName, password);
    
    return "";
}

private void MaximumDayCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MaximumDayCheckBoxActionPerformed
    bMaximumDay=!bMaximumDay;
}//GEN-LAST:event_MaximumDayCheckBoxActionPerformed

private void ThresholdCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ThresholdCheckBoxActionPerformed
    bThreshold=!bThreshold;
}//GEN-LAST:event_ThresholdCheckBoxActionPerformed

private void LSEPriceCapTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LSEPriceCapTextFieldActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_LSEPriceCapTextFieldActionPerformed

private void MaxDayTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MaxDayTextFieldActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_MaxDayTextFieldActionPerformed

private void HostNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HostNameTextFieldActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_HostNameTextFieldActionPerformed

private void ActionProbabilityCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ActionProbabilityCheckBoxActionPerformed
    bActionProbabilityCheck=!bActionProbabilityCheck;
}//GEN-LAST:event_ActionProbabilityCheckBoxActionPerformed

private void StartDayTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StartDayTextFieldActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_StartDayTextFieldActionPerformed

private void CheckDayLengthTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CheckDayLengthTextFieldActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_CheckDayLengthTextFieldActionPerformed

private void ProbabilityDifferenceTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ProbabilityDifferenceTextFieldActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_ProbabilityDifferenceTextFieldActionPerformed

private void LearningParameterCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LearningParameterCheckBoxActionPerformed
    bLearningCheck=!bLearningCheck;
    // TODO add your handling code here:
}//GEN-LAST:event_LearningParameterCheckBoxActionPerformed

private void LearningCheckStartDayTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LearningCheckStartDayTextFieldActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_LearningCheckStartDayTextFieldActionPerformed

private void LearningCheckDayLengthTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LearningCheckDayLengthTextFieldActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_LearningCheckDayLengthTextFieldActionPerformed

private void LearningCheckDifferenceTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LearningCheckDifferenceTextFieldActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_LearningCheckDifferenceTextFieldActionPerformed

private void DailyNetEarningThresholdCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DailyNetEarningThresholdCheckBoxActionPerformed
    bDailyNetEarningThreshold=!bDailyNetEarningThreshold;
    // TODO add your handling code here:
}//GEN-LAST:event_DailyNetEarningThresholdCheckBoxActionPerformed

private void DailyNetEarningStartDayTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DailyNetEarningStartDayTextFieldActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_DailyNetEarningStartDayTextFieldActionPerformed

private void DailyNetEarningDayLengthTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DailyNetEarningDayLengthTextFieldActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_DailyNetEarningDayLengthTextFieldActionPerformed

private void DailyNetEarningsThresholdTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DailyNetEarningsThresholdTextFieldActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_DailyNetEarningsThresholdTextFieldActionPerformed

    private void RandomSeedTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RandomSeedTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_RandomSeedTextField1ActionPerformed

    private void GenerateButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GenerateButton1ActionPerformed
        // TODO add your handling code here:
        Date time=new Date();
        java.util.Random randomSeed = new java.util.Random(time.getTime());
    
        RandomSeed=randomSeed.nextLong();
        RandomSeedTextField1.setText(String.valueOf(RandomSeed));
    }//GEN-LAST:event_GenerateButton1ActionPerformed

    private void PriceSensitiveLSETextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PriceSensitiveLSETextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_PriceSensitiveLSETextField1ActionPerformed

    private void DatabaseNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DatabaseNameTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_DatabaseNameTextFieldActionPerformed

    private void UserNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UserNameTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_UserNameTextFieldActionPerformed

    private void PasswordTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PasswordTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_PasswordTextFieldActionPerformed
    
     
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox ActionProbabilityCheckBox;
    private javax.swing.JButton CancelButton;
    private javax.swing.JTextField CheckDayLengthTextField;
    private javax.swing.JTextField DailyNetEarningDayLengthTextField;
    private javax.swing.JTextField DailyNetEarningStartDayTextField;
    private javax.swing.JCheckBox DailyNetEarningThresholdCheckBox;
    private javax.swing.JLabel DailyNetEarningThresholdTextField;
    private javax.swing.JTextField DailyNetEarningsThresholdTextField;
    private javax.swing.JTextField DatabaseNameTextField;
    private javax.swing.JButton DoneButton;
    private javax.swing.JTextField GenPriceCapTextField;
    private javax.swing.JButton GenerateButton1;
    private javax.swing.JTextField HostNameTextField;
    private javax.swing.JTextField LSEPriceCapTextField;
    private javax.swing.JTextField LearningCheckDayLengthTextField;
    private javax.swing.JTextField LearningCheckDifferenceTextField;
    private javax.swing.JTextField LearningCheckStartDayTextField;
    private javax.swing.JCheckBox LearningParameterCheckBox;
    private javax.swing.JTextField MaxDayTextField;
    private javax.swing.JCheckBox MaximumDayCheckBox;
    private javax.swing.JButton OKButton;
    private javax.swing.JTextField PasswordTextField;
    private javax.swing.JButton PrevButton;
    private javax.swing.JTextField PriceSensitiveLSETextField1;
    private javax.swing.JTextField ProbabilityDifferenceTextField;
    private javax.swing.JTextField RandomSeedTextField1;
    private javax.swing.JTextField StartDayTextField;
    private javax.swing.JCheckBox ThresholdCheckBox;
    private javax.swing.JTextField ThresholdProbabilityTextField;
    private javax.swing.JTextField UserNameTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.ButtonGroup radioButtonGroup;
    // End of variables declaration//GEN-END:variables
    private int iMaxDay;
    private double dThresholdProbability;
    private double dGenPriceCap;
    private double dLSEPriceCap;
    private long RandomSeed;
    private int priceSensitiveLSE;
    private int iStartDay;
    private int iCheckDayLength;
    private double dActionProbability;
    private String hostName;
    private String userName;
    private String databaseName;
    private String password;
    private int iLseData;
   
    private int iLearningCheckStartDay;
    private int iLearningCheckDayLength;
    private double dLearningCheckDifference;
   
    private int iDailyNetEarningStartDay;
    private int iDailyNetEarningDayLength;
    private double dDailyNetEarningThreshold;

    private boolean bMaximumDay=true;
    private boolean bThreshold=true;
    private boolean bDailyNetEarningThreshold=false;
    private boolean bActionProbabilityCheck=false;
    private boolean bLearningCheck=false;

    private boolean bOkSelect;
    private boolean bPrevNextButtonShown;
    
    private AMESFrame amesFrame;
}