package com.Client;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.ClientFactory.Client;
import com.ClientFactory.ClientFactory;
import com.DivergenceSystem.CSVHandler;
import com.DivergenceSystem.ProcessedStudent;
import com.DivergenceSystem.Server;
import com.DivergenceSystem.UndivertedStudent;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
/*
 * Created by JFormDesigner on Wed Apr 24 22:09:42 CST 2024
 */


/**
 * @author liuwy
 */
public class Client_Teacher extends JFrame {
    Client client = null;
    Map<Integer, JSpinner> mapSpinner;
    Map<Integer, JLabel> mapMajorCode2MajorNumber;
    Map<Integer, List<JSlider>> mapMajor2StudentNumber;
    Map<Integer, JSlider> mapClassCode2Number;

    public Client_Teacher() throws IOException {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client = ClientFactory.createClient("teacher");
        mapSpinner = new HashMap<>();
        mapMajorCode2MajorNumber = new HashMap<>();
        mapMajor2StudentNumber = new HashMap<>();
        mapClassCode2Number = new HashMap<>();
        initComponents();
        Init();
    }

    void Init() {
        initMajorInfo();
        initClassInfo();
        showStuFillInfo();
        tabbedPane1.addChangeListener(e -> {
            int selectedIndex = tabbedPane1.getSelectedIndex();
//            System.out.println("切换到Tab页: " + tabbedPane1.getTitleAt(selectedIndex));
            if (selectedIndex == 0) {
                initMajorInfo();
                initClassInfo();
            }
            if (selectedIndex == 2) {
                List<ProcessedStudent> info = client.getPSList();
                showStuProInfo(info);
            }
        });

    }

    public void initMajorInfo() {
        mapSpinner.clear();
        mapMajorCode2MajorNumber.clear();
        mapMajor2StudentNumber.clear();

        List<UndivertedStudent> classList = client.getClassList();
        Map<Integer, Integer> mapMajorCode2Count = new HashMap<>();
        for (UndivertedStudent singleClass : classList) {
            int majorCode = Integer.parseInt(singleClass.gender);
            mapMajorCode2Count.put(majorCode, mapMajorCode2Count.getOrDefault(majorCode, 0) + Integer.parseInt(singleClass.name));
        }

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1));
        JViewport viewport = majorInfo.getViewport();

        List<UndivertedStudent> majorList = client.getMajorClass();
        for (UndivertedStudent major : majorList) {
            if(major.number == -1) continue;
//            System.out.println(major);
            JPanel row = new JPanel(new GridBagLayout());
            row.setPreferredSize(new Dimension(100, 50));
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.gridx = 0;
            gbc.weightx = 1.0;
            gbc.anchor = GridBagConstraints.WEST;
            JLabel label = new JLabel(major.number + "-" + major.name + ": ");
            row.add(label, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.5;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel classNumLabel = new JLabel("  人数：" + String.valueOf(mapMajorCode2Count.getOrDefault(major.number, 0)));
            row.add(classNumLabel, gbc);
            mapMajorCode2MajorNumber.put(major.number, classNumLabel);

            gbc.gridx = 2;
            gbc.weightx = 0.3;
            gbc.anchor = GridBagConstraints.EAST;
            JSpinner spinner = new JSpinner();
            spinner.setValue(Integer.parseInt(major.gender));
            mapSpinner.put(major.number, spinner);
            row.add(spinner, gbc);

            mapMajor2StudentNumber.put(major.number, new ArrayList<>());
            panel.add(row);
        }
        viewport.add(panel);
    }

    public void initClassInfo() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JViewport viewport = classInfo.getViewport();

        List<UndivertedStudent> classList = client.getClassList();
        for (UndivertedStudent singleClass : classList) {
            JPanel row = new JPanel(new BorderLayout());
            JLabel label = new JLabel("班级：" + singleClass.number + "  人数：" + singleClass.name);
            JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 100, Integer.parseInt(singleClass.name));
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);
            slider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int value = slider.getValue(), sum = 0;
                    label.setText("班级：" + singleClass.number + "  人数：" + value);
                    for (JSlider slider : mapMajor2StudentNumber.get(Integer.parseInt(singleClass.gender))) {
                        int val = slider.getValue();
                        sum += val;
                    }
                    mapMajorCode2MajorNumber.get(Integer.parseInt(singleClass.gender)).setText("  人数：" + String.valueOf(sum));
                }
            });
            mapClassCode2Number.put(singleClass.number, slider);
            mapMajor2StudentNumber.get(Integer.parseInt(singleClass.gender)).add(slider);
            row.add(label, BorderLayout.WEST);
            row.add(slider, BorderLayout.EAST);
            panel.add(row);
        }
        viewport.add(panel);
    }

    public void showStuFillInfo() {
        List<UndivertedStudent> info = client.getUSList();
        Map<Integer, String> mapCode2MajorName = new HashMap<>();
        List<UndivertedStudent> majorList = client.getMajorClass();
        for (UndivertedStudent major : majorList) {
            mapCode2MajorName.put(major.number, major.name);
        }
        for (UndivertedStudent us : info) {
            if (us.isFill) {
                us.major_1 = mapCode2MajorName.getOrDefault(Integer.parseInt(us.major_1), "NULL");
                us.major_2 = mapCode2MajorName.getOrDefault(Integer.parseInt(us.major_2), "NULL");
                us.major_3 = mapCode2MajorName.getOrDefault(Integer.parseInt(us.major_3), "NULL");
            }
        }
        // 创建 JTable 组件，并将数据和列名传入
        JTable table1 = new JTable(new UndivertedStudent.DataTableModel(info));
        // 创建 JScrollPane 组件，并将表格作为视图组件添加到其中
        JViewport stuFillInfoViewport = stuFillInfo.getViewport();
        stuFillInfoViewport.add(table1);
        JTable table2 = new JTable(new UndivertedStudent.DataTableModel(info));
        JViewport fillInfoViewport = TP_filInfo.getViewport();
        fillInfoViewport.add(table2);
    }

    public void showStuProInfo(List<ProcessedStudent> info) {
//        List<ProcessedStudent> info = client.getPSList();
        JTable table1 = new JTable(new ProcessedStudent.DataTableModel(info));
        // 创建 JScrollPane 组件，并将表格作为视图组件添加到其中
        JViewport stuFillInfoViewport = TP_result.getViewport();
        stuFillInfoViewport.add(table1);
    }

    private void BT_addMajor(ActionEvent e) {
        String input = JOptionPane.showInputDialog(null, "请输入专业名称:");
        client.addMajor(input);
        initMajorInfo();
    }

    private void BT_delMajor(ActionEvent e) {
        String input = JOptionPane.showInputDialog(null, "请输入专业对应代码:");
        int value = Integer.parseInt(input);
        client.delMajor(value);
        initMajorInfo();
    }

    private void BT_saveMajor(ActionEvent e) {
        for (Map.Entry<Integer, JSpinner> entry : mapSpinner.entrySet()) {
            client.modifyMajor(entry.getKey(), (int) entry.getValue().getValue());
        }
        initMajorInfo();
        client.initClass();
        initClassInfo();
    }

    private void BT_delData(ActionEvent e) {
        client.clearStuInfoWithFill();
        showStuFillInfo();
    }

    private void BT_importData(ActionEvent e) {
        String csvFilePath = "";
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            csvFilePath = fileChooser.getSelectedFile().getAbsolutePath();
        } else {
            return;
        }
        List<UndivertedStudent> usList = CSVHandler.importCSV(csvFilePath);
        client.addUSList(usList);
        showStuFillInfo();
    }

    private void BT_saveClass(ActionEvent e) {
        List<UndivertedStudent> classList = client.getClassList();
        List<UndivertedStudent> classNums = new ArrayList<>();
        for (UndivertedStudent singleClass : classList) {
            int val = mapClassCode2Number.get(singleClass.number).getValue();
            classNums.add(new UndivertedStudent(singleClass.number, String.valueOf(val), "", 0.0));
        }
        client.modifyClass(classNums);
    }

    private void BT_start(ActionEvent e) {
        client.diverge();
        List<ProcessedStudent> info = client.getPSList();
        showStuProInfo(info);
    }

    private void BT_delResult(ActionEvent e) {
        client.clearStuInfoProcessed();
        List<ProcessedStudent> info = client.getPSList();
        showStuProInfo(info);
    }

    private void BT_exportResult(ActionEvent e) {
        String csvFilePath = "";
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            csvFilePath = fileChooser.getSelectedFile().getAbsolutePath();
        } else {
            return;
        }
        List<ProcessedStudent> psList = client.getPSList();
        System.out.println(csvFilePath + "/export_data.csv");
        CSVHandler.exportCSV(csvFilePath + "/export_data.csv", psList);
    }

    private void BT_selByClass(ActionEvent e) {
        String input = JOptionPane.showInputDialog(null, "请输入班级号:");
        int queryClassCode = Integer.parseInt(input);
        List<ProcessedStudent> totalPSList = client.getPSList();
        List<ProcessedStudent> info = new ArrayList<>();
        for(ProcessedStudent ps : totalPSList) {
            if(ps.classCode == queryClassCode)
                info.add(ps);
        }
        showStuProInfo(info);
    }

    private void BT_selByNumber(ActionEvent e) {
        String input = JOptionPane.showInputDialog(null, "请输入学号:");
        int queryNumber = Integer.parseInt(input);
        List<ProcessedStudent> totalPSList = client.getPSList();
        List<ProcessedStudent> info = new ArrayList<>();
        for(ProcessedStudent ps : totalPSList) {
            if(ps.number == queryNumber)
                info.add(ps);
        }
        showStuProInfo(info);
    }

    private void BT_selByMajor(ActionEvent e) {
        String queryMajor = JOptionPane.showInputDialog(null, "请输入专业名称:");
        List<ProcessedStudent> totalPSList = client.getPSList();
        List<ProcessedStudent> info = new ArrayList<>();
        for(ProcessedStudent ps : totalPSList) {
            if(ps.major.equals(queryMajor))
                info.add(ps);
        }
        showStuProInfo(info);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        tabbedPane1 = new JTabbedPane();
        TP_settings = new JPanel();
        majorInfo = new JScrollPane();
        BT_addMajor = new JButton();
        BT_delMajor = new JButton();
        BT_saveMajor = new JButton();
        classInfo = new JScrollPane();
        BT_delData = new JButton();
        BT_importData = new JButton();
        BT_exportData = new JButton();
        stuFillInfo = new JScrollPane();
        BT_saveClass = new JButton();
        TP_filInfo = new JScrollPane();
        panel1 = new JPanel();
        BT_exportResult = new JButton();
        TP_result = new JScrollPane();
        BT_start = new JButton();
        BT_delResult = new JButton();
        BT_selByClass = new JButton();
        BT_selByNumber = new JButton();
        BT_selByMajor = new JButton();

        //======== this ========
        var contentPane = getContentPane();
        contentPane.setLayout(null);

        //======== tabbedPane1 ========
        {
            tabbedPane1.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 20));

            //======== TP_settings ========
            {
                TP_settings.setLayout(null);
                TP_settings.add(majorInfo);
                majorInfo.setBounds(30, 75, 400, 195);

                //---- BT_addMajor ----
                BT_addMajor.setText("\u6dfb\u52a0\u4e13\u4e1a");
                BT_addMajor.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 20));
                BT_addMajor.addActionListener(e -> BT_addMajor(e));
                TP_settings.add(BT_addMajor);
                BT_addMajor.setBounds(25, 15, 130, 55);

                //---- BT_delMajor ----
                BT_delMajor.setText("\u5220\u9664\u4e13\u4e1a");
                BT_delMajor.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 20));
                BT_delMajor.addActionListener(e -> BT_delMajor(e));
                TP_settings.add(BT_delMajor);
                BT_delMajor.setBounds(160, 15, 130, 55);

                //---- BT_saveMajor ----
                BT_saveMajor.setText("\u4fdd\u5b58\u4e13\u4e1a");
                BT_saveMajor.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 20));
                BT_saveMajor.addActionListener(e -> BT_saveMajor(e));
                TP_settings.add(BT_saveMajor);
                BT_saveMajor.setBounds(300, 15, 130, 55);
                TP_settings.add(classInfo);
                classInfo.setBounds(30, 275, 400, 440);

                //---- BT_delData ----
                BT_delData.setText("\u6e05\u9664\u6570\u636e");
                BT_delData.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
                BT_delData.addActionListener(e -> BT_delData(e));
                TP_settings.add(BT_delData);
                BT_delData.setBounds(125, 720, 90, 50);

                //---- BT_importData ----
                BT_importData.setText("\u5bfc\u5165\u6570\u636e");
                BT_importData.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
                BT_importData.addActionListener(e -> BT_importData(e));
                TP_settings.add(BT_importData);
                BT_importData.setBounds(225, 720, 90, 50);

                //---- BT_exportData ----
                BT_exportData.setText("\u5bfc\u51fa\u6570\u636e");
                BT_exportData.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
                TP_settings.add(BT_exportData);
                BT_exportData.setBounds(325, 720, 90, 50);
                TP_settings.add(stuFillInfo);
                stuFillInfo.setBounds(445, 20, 675, 750);

                //---- BT_saveClass ----
                BT_saveClass.setText("\u4fdd\u5b58\u73ed\u7ea7");
                BT_saveClass.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
                BT_saveClass.addActionListener(e -> BT_saveClass(e));
                TP_settings.add(BT_saveClass);
                BT_saveClass.setBounds(25, 720, 90, 50);
            }
            tabbedPane1.addTab("\u8bbe\u7f6e", TP_settings);
            tabbedPane1.addTab("\u5b66\u751f\u586b\u62a5\u4fe1\u606f", TP_filInfo);

            //======== panel1 ========
            {
                panel1.setLayout(null);

                //---- BT_exportResult ----
                BT_exportResult.setText("\u5bfc\u51fa\u6570\u636e");
                BT_exportResult.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 20));
                BT_exportResult.addActionListener(e -> BT_exportResult(e));
                panel1.add(BT_exportResult);
                BT_exportResult.setBounds(150, 15, 120, 40);
                panel1.add(TP_result);
                TP_result.setBounds(20, 60, 1100, 710);

                //---- BT_start ----
                BT_start.setText("\u5f00\u59cb\u5206\u6d41");
                BT_start.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 20));
                BT_start.addActionListener(e -> BT_start(e));
                panel1.add(BT_start);
                BT_start.setBounds(20, 15, 120, 40);

                //---- BT_delResult ----
                BT_delResult.setText("\u6e05\u7a7a\u5206\u6d41\u4fe1\u606f");
                BT_delResult.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 20));
                BT_delResult.addActionListener(e -> BT_delResult(e));
                panel1.add(BT_delResult);
                BT_delResult.setBounds(280, 15, 160, 40);

                //---- BT_selByClass ----
                BT_selByClass.setText("\u73ed\u7ea7\u7b5b\u9009");
                BT_selByClass.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 20));
                BT_selByClass.addActionListener(e -> BT_selByClass(e));
                panel1.add(BT_selByClass);
                BT_selByClass.setBounds(445, 15, 120, 40);

                //---- BT_selByNumber ----
                BT_selByNumber.setText("\u5b66\u53f7\u7b5b\u9009");
                BT_selByNumber.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 20));
                BT_selByNumber.addActionListener(e -> BT_selByNumber(e));
                panel1.add(BT_selByNumber);
                BT_selByNumber.setBounds(570, 15, 120, 40);

                //---- BT_selByMajor ----
                BT_selByMajor.setText("\u4e13\u4e1a\u7b5b\u9009");
                BT_selByMajor.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 20));
                BT_selByMajor.addActionListener(e -> BT_selByMajor(e));
                panel1.add(BT_selByMajor);
                BT_selByMajor.setBounds(695, 15, 120, 40);
            }
            tabbedPane1.addTab("\u5b66\u751f\u5206\u6d41\u4fe1\u606f", panel1);
        }
        contentPane.add(tabbedPane1);
        tabbedPane1.setBounds(45, 10, 1130, 830);

        contentPane.setPreferredSize(new Dimension(960, 721));
        setSize(1200, 901);
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JTabbedPane tabbedPane1;
    private JPanel TP_settings;
    private JScrollPane majorInfo;
    private JButton BT_addMajor;
    private JButton BT_delMajor;
    private JButton BT_saveMajor;
    private JScrollPane classInfo;
    private JButton BT_delData;
    private JButton BT_importData;
    private JButton BT_exportData;
    private JScrollPane stuFillInfo;
    private JButton BT_saveClass;
    private JScrollPane TP_filInfo;
    private JPanel panel1;
    private JButton BT_exportResult;
    private JScrollPane TP_result;
    private JButton BT_start;
    private JButton BT_delResult;
    private JButton BT_selByClass;
    private JButton BT_selByNumber;
    private JButton BT_selByMajor;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
