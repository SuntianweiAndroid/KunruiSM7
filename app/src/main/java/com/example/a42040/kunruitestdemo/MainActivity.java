package com.example.a42040.kunruitestdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.serialport.SerialPortSpd;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.UHFManager;
import com.speedata.libutils.DataConversionUtils;
import com.uhf.linkage.Linkage;
import com.uhf.structures.InventoryData;
import com.uhf.structures.KrSm7Data;
import com.uhf.structures.OnInventoryListener;
import com.uhf.structures.OnReadWriteListener;
import com.uhf.structures.RW_Params;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class MainActivity extends Activity implements View.OnClickListener, OnReadWriteListener, OnInventoryListener {
    private SerialPortSpd serialPort;
    private int fd;
    private TextView mTvVersions, mTvepc;
    private IUHFService iuhfService;

    /**
     * 获取版本号
     */
    private Button mBtnReadVersions;
    /**
     * SM7验证
     */
    private Button mBtnSm7Check;
    /**
     * 写epc
     */
    private Button mBtnUhfWriteEpc;
    private TextView mmEdtMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        init();
    }

    /**
     * 初始化uhf与sm7加密芯片串口
     */
    private void init() {
        EventBus.getDefault().register(this);

        try {
            serialPort = new SerialPortSpd();
            serialPort.OpenSerial(SerialPortSpd.SERIAL_TTYMT1, 38400);
            fd = serialPort.getFd();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            iuhfService = UHFManager.getUHFService(MainActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
            boolean cn = getApplicationContext().getResources().getConfiguration().locale.getCountry().equals("CN");
            if (cn) {
                Toast.makeText(getApplicationContext(), "模块不存在", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "模块不存在", Toast.LENGTH_SHORT).show();
            }
            return;
        }
    }


    /**
     * 上电开串口
     *
     * @return
     */
    private boolean openDev() {
        if (iuhfService.openDev() != 0) {
//            Cur_Tag_Info.setText("Open serialport failed");
//            new AlertDialog.Builder(this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_OPEN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
//
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//
//                    finish();
//                }
//            }).show();
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.btn_uhf_write_epc:
//                RW_Params w_params = new RW_Params();
//                int write_status = linkage.Radio_WriteTagSync(1, 2, 1, DataConversionUtils.HexString2Bytes("00000000"), DataConversionUtils.hexStringToByteArray("1111"), 3000, w_params);
//                if (write_status == 0) {
//                    if (w_params.status == 0) {
//                    }
//                }
//                int Read_status = linkage.Radio_readTagSync(6, 2, 1, DataConversionUtils.HexString2Bytes("00000000"), 3000, w_params);
//                int sta = linkage.krSm7Write(6, 0, 1, DataConversionUtils.HexString2Bytes("00000000"), DataConversionUtils.hexStringToByteArray("666666666666666666666666"));
//                mmEdtMsg.append("\n写epc区数据：" + "666666666666666666666666");
//                if (sta == 0) {
//                    KrSm7Data krSm7Datas = new KrSm7Data();
//                    sta = linkage.krSm7Read(6, 0, 1, DataConversionUtils.HexString2Bytes("00000000"), krSm7Datas);
//                    if (sta == 0) {
//                        mmEdtMsg.append("\n读epc区成功返回数据：" + DataConversionUtils.byteArrayToString(krSm7Datas.Sm7Data));
//                    }
//                }
                break;
            case R.id.btn_read_versions:
                String versions = "EE00030AA996";
                serialPort.WriteSerialByte(fd, DataConversionUtils.HexString2Bytes(versions.toUpperCase()));
                SystemClock.sleep(300);
                try {
                    byte[] resultBytes = serialPort.ReadSerial(fd, 128);
                    if (resultBytes != null) {
                        byte[] re = cutBytes(resultBytes, 4, 1);
                        if (DataConversionUtils.byteArrayToInt(re) == 0) {
                            re = cutBytes(resultBytes, 9, 2);
                            switch (DataConversionUtils.byteArrayToString(re)) {
                                case "0000":
                                    byte[] rVersions = cutBytes(resultBytes, 5, 2);
                                    byte[] yVersions = cutBytes(resultBytes, 7, 2);
                                    String rv = DataConversionUtils.byteArrayToString(rVersions);
                                    String yv = DataConversionUtils.byteArrayToString(yVersions);
                                    mTvVersions.setText("软件版本:V." + rv + "       硬件版本:V." + rv);
                                    break;
                                case "0001":
                                    mTvVersions.setText("CRC校验错误");
                                    break;
                                case "abcd":
                                    mTvVersions.setText("未知错误");
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            mTvVersions.setText("校验错误");
                        }

                    } else {
                        mTvVersions.setText("读取错误");
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_sm7_check:
                Sm7_ZflagCheck.checkFlag2();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (openDev()) {
                Toast.makeText(this, "uhf初始化失败", Toast.LENGTH_SHORT).show();
                return;
            }
            if (iuhfService != null) {
                Toast.makeText(this, "uhf初始化成功", Toast.LENGTH_SHORT).show();
                Sm7_ZflagCheck.initCheck(this, iuhfService);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (iuhfService != null) {
                iuhfService.closeDev();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initView() {
        mTvVersions = findViewById(R.id.tv_versions);
        mBtnReadVersions = findViewById(R.id.btn_read_versions);
        mBtnReadVersions.setOnClickListener(this);
        mBtnSm7Check = findViewById(R.id.btn_sm7_check);
        mBtnSm7Check.setOnClickListener(this);
        mBtnUhfWriteEpc = findViewById(R.id.btn_uhf_write_epc);
        mBtnUhfWriteEpc.setOnClickListener(this);
        mmEdtMsg = findViewById(R.id.edt_msg);
        mTvepc = findViewById(R.id.tv_epc);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MsgEvent mEvent) {
        String type = mEvent.getType();
        String msg = (String) mEvent.getMsg();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ResultBeen resultBeen) {
        Log.i("tw", "onEventMainThread: " + resultBeen.getLogInfo());
        mTvepc.setText(resultBeen.getEpc());
        if (resultBeen.getResult() == 0) {

            mmEdtMsg.setText("验证成功！");
        } else {
            mmEdtMsg.setText("验证失败！");
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
    public static byte[] cutBytes(byte[] bytes, int start, int length) {
        byte[] res = new byte[length];
        System.arraycopy(bytes, start, res, 0, length);
        return res;
    }

    @Override
    public void getReadWriteData(RW_Params rw_params) {
        if (rw_params.type == 2) {//读
            byte[] resultData = new byte[rw_params.EPCLen];
            try {
                byte[] epcData = rw_params.EPCData;
//                System.arraycopy(epcData, 0, resultData, 0, rw_params.EPCLen);
                mmEdtMsg.append("\n读epc区成功返回数据：" + DataConversionUtils.byteArrayToString(epcData));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (rw_params.type == 3) {//写
        }

    }

    @Override
    public void getInventoryData(InventoryData inventoryData) {

    }
}
