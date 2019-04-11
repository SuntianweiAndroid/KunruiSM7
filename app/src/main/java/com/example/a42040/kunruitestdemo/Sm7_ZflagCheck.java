package com.example.a42040.kunruitestdemo;

import android.content.Context;
import android.os.SystemClock;
import android.serialport.SerialPortBackup;
import android.util.Log;

import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.utils.DataConversionUtils;
import com.uhf.structures.InventoryData;
import com.uhf.structures.KrSm7Data;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Random;

public class Sm7_ZflagCheck {
    private static IUHFService iuhfService;
    private static Context mContext;
    private static SerialPortBackup serialPort;
    private static int fd;
    private static String epcs = "";
    private static StringBuilder Status;

    public static void initCheck(Context context, IUHFService iuhfService) {
        Sm7_ZflagCheck.iuhfService = iuhfService;
        mContext = context;
        try {
            serialPort = new SerialPortBackup();
            serialPort.OpenSerial(SerialPortBackup.SERIAL_TTYMT1, 38400);
            fd = serialPort.getFd();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void checkFlag() {
        epcs = "";
        Status = new StringBuilder();
        InventoryData inventoryData = new InventoryData();
        int result = iuhfService.krSm7Inventory(inventoryData);
        if (result == 0) {
            epcs = DataConversionUtils.byteArrayToString(inventoryData.EPC_Data);
            Status.append("盘点epc数据" + epcs);
            EventBus.getDefault().post(new ResultBeen(0, Status.toString(), epcs));
            //###---Z型标签添加---###
//            result = iuhfService.selectCard(1, epcs, true);
//            if (result == 0) {
//                //读取tid
//                byte[] tidDatas = iuhfService.read_area(2, 0, 6, "00000000");
//                if (tidDatas != null) {
//                    String tidDatasStr = DataConversionUtils.byteArrayToString(tidDatas).toUpperCase();
//                    if (tidDatasStr.substring(2, 5).equals("0A1")) {
//                        Status.append("\nZ型标签验证成功");
//                        EventBus.getDefault().post(new ResultBeen(0, Status.toString(), epcs));
//                    } else {
//                        Status.append("\nZ型标签验证失败");
//                        EventBus.getDefault().post(new ResultBeen(1, Status.toString(), epcs));
////                        sm7check("00000000", "00000000000000000000000000000000", "01");
//                        return;
//                    }
//                } else {
//                    Status.append("\n读取TID失败请重试");
//                    sm7check("00000000", "00000000000000000000000000000000", "01");
//                }
//            } else {
//                Status.append("\n选卡失败请重试");
//                EventBus.getDefault().post(new ResultBeen(1, Status.toString(), epcs));
//            }
            sm7check("00000000", "00000000000000000000000000000000", "01");
            //###---Z型标签添加---###
        } else {
            Status.append("\n盘点错误请重试");
            EventBus.getDefault().post(new ResultBeen(1, Status.toString(), epcs));
        }
    }

    public static void checkFlag2() {
        epcs = "";
        Status = new StringBuilder();
        InventoryData inventoryData = new InventoryData();
        int result = iuhfService.krSm7Inventory(inventoryData);
        if (result == 0) {
            epcs = DataConversionUtils.byteArrayToString(inventoryData.EPC_Data);
            //读取tid
            byte[] tidDatas = iuhfService.read_area(2, 0, 6, "00000000");
            if (tidDatas != null) {
                String tidDatasStr = DataConversionUtils.byteArrayToString(tidDatas).toUpperCase();
                String sss = tidDatasStr.substring(2, 5);
                if (tidDatasStr.substring(2, 5).equals("0A1")) {
                    Status.append("\nZ型标签验证成功");
                    EventBus.getDefault().post(new ResultBeen(0, Status.toString(), epcs));
                } else {
                    Status.append("\nZ型标签验证失败");
                    EventBus.getDefault().post(new ResultBeen(1, Status.toString(), epcs));
                }
            } else {
                //重新inventory+selectCard
                result = iuhfService.krSm7Inventory(inventoryData);
                if (result == 0) {
                    sm7check("00000000", "00000000000000000000000000000000", "01");
                    // 判断sm7check的值，error-验证失败  true-验证成功。
                } else {
                    Log.e("tw", "盘点失败");
                    Status.append("\n盘点失败请重试");
                }
            }
        } else {
            Log.e("tw", "盘点失败");
            Status.append("\n盘点失败请重试");
//            sm7check("00000000", "00000000000000000000000000000000", "01");
        }
    }

    public static void checkFlag3() {
        epcs = "";
        InventoryData inventoryData = new InventoryData();
        int result = iuhfService.krSm7Inventory(inventoryData);
        if (result == 0) {
            epcs = DataConversionUtils.byteArrayToString(inventoryData.EPC_Data);
            Status.append("盘点epc数据" + epcs);
            EventBus.getDefault().post(new ResultBeen(0, Status.toString(), epcs));
            //###---Z型标签添加---###
            result = iuhfService.selectCard(1, epcs, true);
            if (result == 0) {
                //读取tid
                byte[] tidDatas = iuhfService.read_area(2, 0, 6, "00000000");
                if (tidDatas != null) {
                    String tidDatasStr = DataConversionUtils.byteArrayToString(tidDatas).toUpperCase();
                    if (tidDatasStr.substring(2, 5).equals("0A1")) {
                        Status.append("\nZ型标签验证成功");
                        EventBus.getDefault().post(new ResultBeen(0, Status.toString(), epcs));
                    } else {
                        Status.append("\nZ型标签验证失败");
                        EventBus.getDefault().post(new ResultBeen(1, Status.toString(), epcs));
                        sm7check("00000000", "00000000000000000000000000000000", "01");
                    }
                } else {
                    //重新inventory+selectCard
                    result = iuhfService.krSm7Inventory(inventoryData);
                    if (result == 0) {
                        epcs = DataConversionUtils.byteArrayToString(inventoryData.EPC_Data);
                        Status.append("盘点epc数据" + epcs);
                        EventBus.getDefault().post(new ResultBeen(0, Status.toString(), epcs));
                        result = iuhfService.selectCard(1, epcs, true);
                        if (result == 0) {
                            sm7check("00000000", "00000000000000000000000000000000", "01");
                            // 判断sm7check的值，error-验证失败  true-验证成功。
                        } else {
                            Log.e("tw", "选卡失败");
                            Status.append("\n选卡失败请重试");
                        }
                    } else {
                        Log.e("tw", "盘点失败");
                    }
                }
            } else {
                Log.e("tw", "选卡失败");
                Status.append("\n选卡失败请重试");
//            sm7check("00000000", "00000000000000000000000000000000", "01");
            }
            //###---Z型标签添加---###
        } else {
            Status.append("\n盘点错误请重试");
            EventBus.getDefault().post(new ResultBeen(1, Status.toString(), epcs));
            Log.i("sm7", "盘点错误");
        }
    }

    /**
     * 坤锐sm7验证
     *
     * @param pwd
     */
    private static void sm7check(String pwd, String krsm7pwd, String krsm7action) {
        //blockwrite写uhf-EPC
        String random = DataConversionUtils.byteArrayToString(getRandombyte());
//            String random = "0a0a0a0a";
        int result1 = iuhfService.krSm7Blockwrite(2, 96, 1, DataConversionUtils.HexString2Bytes(pwd), DataConversionUtils.HexString2Bytes(random));
        if (result1 == 0) {
            Status.append("\nToken1：" + random);
            KrSm7Data krSm7Data = new KrSm7Data();
            //sm7读UHF-EPC  读60h区4个word（8个byte）
            result1 = iuhfService.krSm7Read(4, 96, 1, DataConversionUtils.HexString2Bytes(pwd), krSm7Data);
            if (result1 == 0) {
                Status.append("\nToken1返回：" + DataConversionUtils.byteArrayToString(krSm7Data.Sm7Data));
                if (krSm7Data.Sm7Data.length != 0) {
                    //解密
//                        String sm7Data = "EE001C060000000000000000000000000000000001"
                    String sm7Data = "EE001C06" + krsm7pwd + krsm7action
                            + DataConversionUtils.byteArrayToString(krSm7Data.Sm7Data).toUpperCase();
                    String crc = Integer.toHexString(CalCRC16(DataConversionUtils.HexString2Bytes(sm7Data)));
                    sm7Data = sm7Data + crc.toUpperCase();
                    byte[] sm7DataBytes = DataConversionUtils.HexString2Bytes(sm7Data);
                    Status.append("\nToken2：" + DataConversionUtils.byteArrayToString(sm7DataBytes));
                    serialPort.WriteSerialByte(fd, sm7DataBytes);
                    SystemClock.sleep(300);
                    try {
                        byte[] re = serialPort.ReadSerial(fd, 128);
                        if (re != null) {
                            byte[] status = cutBytes(re, 4, 1);
                            if (DataConversionUtils.byteArrayToInt(status) == 0) {
                                byte[] statusCode = cutBytes(re, 13, 2);
                                switch (DataConversionUtils.byteArrayToString(statusCode)) {
                                    case "0000":
                                        //no error
                                        Status.append("\nToken2返回：" + DataConversionUtils.byteArrayToString(re));
                                        byte[] datas = cutBytes(re, 9, 4);
                                        //验证初始随机数 与加密芯片返回的数据是否一样
                                        if (random.equals(DataConversionUtils.byteArrayToString(cutBytes(re, 5, 4)))) {
                                            Status.append("\nToken3：" + DataConversionUtils.byteArrayToString(datas));
                                            result1 = iuhfService.krSm7Blockwrite(4, 97, 1,
                                                    DataConversionUtils.HexString2Bytes(pwd), datas);
                                            if (result1 == 0) {
                                                Status.append("\n验证成功！！");
                                                EventBus.getDefault().post(new ResultBeen(0, Status.toString(), epcs));
                                            } else {
                                                Status.append("\n验证失败！！");
                                                EventBus.getDefault().post(new ResultBeen(1, Status.toString(), epcs));
                                            }
                                        } else {
                                            Status.append("\n验证失败！！");
                                            EventBus.getDefault().post(new ResultBeen(1, Status.toString(), epcs));
                                        }
                                        break;
                                    case "0001":
                                        //CRC 校验错
                                        Status.append("\n加密芯片返回CRC 校验错");
                                        EventBus.getDefault().post(new ResultBeen(1, Status.toString(), epcs));
//                                        Log.i("sm7", "onClick: CRC 校验错");
                                        break;
                                    case "0002":
                                        //load key error
                                        Status.append("\n加密芯片返回load key error");
                                        EventBus.getDefault().post(new ResultBeen(1, Status.toString(), epcs));
//                                        Log.i("sm7", "onClick: load key error");
                                        break;
                                    case "0003":
                                        // 加解密错
                                        Status.append("\n加密芯片返回加解密错");
                                        EventBus.getDefault().post(new ResultBeen(1, Status.toString(), epcs));
//                                        Log.i("sm7", "onClick: 加解密错");
                                        break;
                                    case "abcd":
                                        //未知错误
                                        Status.append("\n加密芯片返回未知错误");
                                        EventBus.getDefault().post(new ResultBeen(1, Status.toString(), epcs));
//                                        Log.i("sm7", "onClick: 未知错误");
                                        break;
                                    default:
                                        break;
                                }
                            } else {
                                Status.append("\n加密芯片返回错误");
                                EventBus.getDefault().post(new ResultBeen(1, Status.toString(), epcs));
//                                Log.i("sm7", "返回错误");
                            }
                        } else {
                            Status.append("\n串口返回null");
                            EventBus.getDefault().post(new ResultBeen(1, Status.toString(), epcs));
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
//                    Log.i("sm7", "onClick: 读60h-uhf读返回空");
                    EventBus.getDefault().post(new ResultBeen(1, Status.toString(), epcs));
                }
            } else {
                Status.append("\n读60H错误");
                EventBus.getDefault().post(new ResultBeen(1, Status.toString(), epcs));
            }
        } else {
            Status.append("\n写60H错误");
            EventBus.getDefault().post(new ResultBeen(1, Status.toString(), epcs));
        }

    }

    /**
     * 截取数组
     *
     * @param bytes  被截取数组
     * @param start  被截取数组开始截取位置
     * @param length 新数组的长度
     * @return 新数组
     */
    private static byte[] cutBytes(byte[] bytes, int start, int length) {
        byte[] res = new byte[length];
        System.arraycopy(bytes, start, res, 0, length);
        return res;
    }

    /**
     * crc16校验
     *
     * @param buf
     * @return
     */
    private static char CalCRC16(byte[] buf) {
        char crcSeed = 0xFFFF;
        char crc = crcSeed;
        for (int i = 0; i < buf.length; i++) {
            crc = (char) ((buf[i] << 8) ^ crc);
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x8000) == 0x8000) {
                    crc = (char) (crc - 0x8000);
                    crc = (char) ((crc << 1) ^ 0x1021);
                } else {
                    crc <<= 1;
                }
            }
        }
        crc = (char) (crc ^ crcSeed);
        return crc;
    }

    /**
     * 获取随机byte[]
     *
     * @return
     */
    private static byte[] getRandombyte() {
        Random random = new Random();
        byte[] b = {1, 2, 3, 4};
        for (int i = 0; i < 4; i++) {
            Integer is = random.nextInt(9);
            b[i] = Byte.parseByte(is.toString());
        }
        return b;
    }
}
