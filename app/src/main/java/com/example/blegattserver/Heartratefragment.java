//Yehiya 27.09.19
package com.example.blegattserver;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Heartratefragment extends ServiceFragment  {
    private static final String TAG = Heartratefragment.class.getCanonicalName();
    private static final int MIN_UINT = 0;
    private static final int MAX_UINT8 = (int) Math.pow(2, 8) - 1;
    private static final int MAX_UINT16 = (int) Math.pow(2, 16) - 1;
    private static final UUID HEART_RATE_SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb");
    private static final UUID HEART_RATE_MEASUREMENT_UUID = UUID.fromString("00002A7A-0000-1000-8000-00805f9b34fb");
    private static final int HEART_RATE_MEASUREMENT_VALUE_FORMAT = BluetoothGattCharacteristic.FORMAT_UINT8;
    private static final int INITIAL_HEART_RATE_MEASUREMENT_VALUE =30;
    private static final int EXPENDED_ENERGY_FORMAT = BluetoothGattCharacteristic.FORMAT_UINT16;
    private static final int INITIAL_EXPENDED_ENERGY = 0;
    private static final String HEART_RATE_MEASUREMENT_DESCRIPTION = "Used to send a heart rate " + "measurement";
    private static final UUID BODY_SENSOR_LOCATION_UUID = UUID.fromString("00002A38-0000-1000-8000-00805f9b34fb");
    private static final int LOCATION_OTHER = 0;
    private static final UUID HEART_RATE_CONTROL_POINT_UUID = UUID.fromString("00002A39-0000-1000-8000-00805f9b34fb");
    private BluetoothGattService mHeartRateService;
    private BluetoothGattCharacteristic mHeartRateMeasurementCharacteristic ,mHeartRateMeasurementCharacteristic1;
    private BluetoothGattCharacteristic mBodySensorLocationCharacteristic;
    private BluetoothGattCharacteristic mHeartRateControlPoint;
    private EditText mEditTextHeartRateMeasurement;
    private ServiceFragmentDelegate mDelegate;
    private RadioButton Device_Registration,Lock_Activity,Health_Status,Low_Battery,Login_Attempts_Failure,Improper_Shaft_Movement,Device_Over_Heat,Preventive_Maintenance,Tampering_Attempt;




    private final OnEditorActionListener mOnEditorActionListenerHeartRateMeasurement = new OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String newHeartRateMeasurementValueString = textView.getText().toString();
                if (isValidCharacteristicValue(newHeartRateMeasurementValueString,
                        HEART_RATE_MEASUREMENT_VALUE_FORMAT)) {
                    int newHeartRateMeasurementValue = Integer.parseInt(newHeartRateMeasurementValueString);
                    mHeartRateMeasurementCharacteristic.setValue(newHeartRateMeasurementValue, HEART_RATE_MEASUREMENT_VALUE_FORMAT,/* offset */ 1);
                    mHeartRateMeasurementCharacteristic1.setValue(newHeartRateMeasurementValue, HEART_RATE_MEASUREMENT_VALUE_FORMAT,/* offset */ 1);


                }
                else {
                    Toast.makeText(getActivity(), R.string.heartRateMeasurementValueInvalid,
                            Toast.LENGTH_SHORT).show();
                }
            }
            return false;
        }
    };

    private final OnEditorActionListener mOnEditorActionListenerEnergyExpended = new OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String newEnergyExpendedString = textView.getText().toString();
                if (isValidCharacteristicValue(newEnergyExpendedString,
                        EXPENDED_ENERGY_FORMAT)) {
                    int newEnergyExpended = Integer.parseInt(newEnergyExpendedString);
                    mHeartRateMeasurementCharacteristic.setValue(newEnergyExpended, EXPENDED_ENERGY_FORMAT,/* offset */ 2); }

                else {
                    Toast.makeText(getActivity(), R.string.energyExpendedInvalid,
                            Toast.LENGTH_SHORT).show();
                }
            }
            return false;
        }
    };
    private EditText mEditTextEnergyExpended;
    private Spinner mSpinnerBodySensorLocation;
    private final OnItemSelectedListener mLocationSpinnerOnItemSelectedListener =
            new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    setBodySensorLocationValue(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            };



    private final OnClickListener DeviceregistrationListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            setHeartRateMeasurementValue(INITIAL_HEART_RATE_MEASUREMENT_VALUE, INITIAL_EXPENDED_ENERGY);
            mDelegate.sendNotificationToDevices(mHeartRateMeasurementCharacteristic);
        }
    };

    private final OnClickListener Lock_ActivityListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            setHeartRateMeasurementValue1(INITIAL_HEART_RATE_MEASUREMENT_VALUE, INITIAL_EXPENDED_ENERGY);
            mDelegate.sendNotificationToDevices(mHeartRateMeasurementCharacteristic);
        }
    };


    private final OnClickListener Healthstatuslistener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            setHeartRateMeasurementValue2(INITIAL_HEART_RATE_MEASUREMENT_VALUE, INITIAL_EXPENDED_ENERGY);
            mDelegate.sendNotificationToDevices(mHeartRateMeasurementCharacteristic);
        }
    };

    private final OnClickListener Low_Batterylistener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            setHeartRateMeasurementValue3(INITIAL_HEART_RATE_MEASUREMENT_VALUE, INITIAL_EXPENDED_ENERGY);
            mDelegate.sendNotificationToDevices(mHeartRateMeasurementCharacteristic);
        }
    };


    private final OnClickListener Login_Attempts_Failurelistener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            setHeartRateMeasurementValue4(INITIAL_HEART_RATE_MEASUREMENT_VALUE, INITIAL_EXPENDED_ENERGY);
            mDelegate.sendNotificationToDevices(mHeartRateMeasurementCharacteristic);
        }
    };

    private final OnClickListener Improper_Shaft_Movementlistener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            setHeartRateMeasurementValue5(INITIAL_HEART_RATE_MEASUREMENT_VALUE, INITIAL_EXPENDED_ENERGY);
            mDelegate.sendNotificationToDevices(mHeartRateMeasurementCharacteristic);
        }
    };

    private final OnClickListener Device_Over_Heatlistener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            setHeartRateMeasurementValue6(INITIAL_HEART_RATE_MEASUREMENT_VALUE, INITIAL_EXPENDED_ENERGY);
            mDelegate.sendNotificationToDevices(mHeartRateMeasurementCharacteristic);
        }
    };

    private final OnClickListener Preventive_Maintenancelistener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            setHeartRateMeasurementValue7(INITIAL_HEART_RATE_MEASUREMENT_VALUE, INITIAL_EXPENDED_ENERGY);
            mDelegate.sendNotificationToDevices(mHeartRateMeasurementCharacteristic);
        }
    };

    private final OnClickListener Tampering_Attemptlistener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            setHeartRateMeasurementValue8(INITIAL_HEART_RATE_MEASUREMENT_VALUE, INITIAL_EXPENDED_ENERGY);
            mDelegate.sendNotificationToDevices(mHeartRateMeasurementCharacteristic);
        }
    };

    public Heartratefragment() {
        mHeartRateMeasurementCharacteristic = new BluetoothGattCharacteristic(HEART_RATE_MEASUREMENT_UUID, BluetoothGattCharacteristic.PROPERTY_NOTIFY,/* No permissions */ 0);
        mHeartRateMeasurementCharacteristic.addDescriptor(Peripheral.getClientCharacteristicConfigurationDescriptor());
        mHeartRateMeasurementCharacteristic.addDescriptor(Peripheral.getCharacteristicUserDescriptionDescriptor(HEART_RATE_MEASUREMENT_DESCRIPTION));
        mBodySensorLocationCharacteristic = new BluetoothGattCharacteristic(BODY_SENSOR_LOCATION_UUID, BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ);
        mHeartRateControlPoint = new BluetoothGattCharacteristic(HEART_RATE_CONTROL_POINT_UUID, BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE);
        mHeartRateService = new BluetoothGattService(HEART_RATE_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        mHeartRateService.addCharacteristic(mHeartRateMeasurementCharacteristic);
        mHeartRateService.addCharacteristic(mBodySensorLocationCharacteristic);
        mHeartRateService.addCharacteristic(mHeartRateControlPoint);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.activity_heartratefragment, container, false);
        mSpinnerBodySensorLocation = (Spinner) view.findViewById(R.id.spinner_bodySensorLocation);
        mSpinnerBodySensorLocation.setOnItemSelectedListener(mLocationSpinnerOnItemSelectedListener);
        mEditTextHeartRateMeasurement = (EditText) view.findViewById(R.id.editText_heartRateMeasurementValue);
        mEditTextHeartRateMeasurement.setOnEditorActionListener(mOnEditorActionListenerHeartRateMeasurement);
        mEditTextEnergyExpended = (EditText) view.findViewById(R.id.editText_energyExpended);
        mEditTextEnergyExpended.setOnEditorActionListener(mOnEditorActionListenerEnergyExpended);


        RadioButton Device_Registration = (RadioButton) view.findViewById(R.id.RB_1);
        RadioButton Lock_Activity = (RadioButton) view.findViewById(R.id.RB_2);
        RadioButton Health_Status = (RadioButton) view.findViewById(R.id.RB_3);
        RadioButton Low_Battery = (RadioButton) view.findViewById(R.id.RB_4);
        RadioButton Login_Attempts_Failure = (RadioButton) view.findViewById(R.id.RB_5);
        RadioButton Improper_Shaft_Movement = (RadioButton) view.findViewById(R.id.RB_6);
        RadioButton Device_Over_Heat = (RadioButton) view.findViewById(R.id.RB_7);
        RadioButton Preventive_Maintenance = (RadioButton) view.findViewById(R.id.RB_8);
        RadioButton Tampering_Attempt = (RadioButton) view.findViewById(R.id.RB_9);

        Device_Registration.setOnClickListener(DeviceregistrationListener);
        Lock_Activity.setOnClickListener(Lock_ActivityListener);
        Health_Status.setOnClickListener(Healthstatuslistener);
        Low_Battery.setOnClickListener(Low_Batterylistener);
        Login_Attempts_Failure.setOnClickListener(Login_Attempts_Failurelistener);
        Improper_Shaft_Movement.setOnClickListener(Improper_Shaft_Movementlistener);
        Device_Over_Heat.setOnClickListener(Device_Over_Heatlistener);
        Preventive_Maintenance.setOnClickListener(Preventive_Maintenancelistener);
        Tampering_Attempt.setOnClickListener(Tampering_Attemptlistener);

        setBodySensorLocationValue(LOCATION_OTHER);
        return view;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mDelegate = (ServiceFragmentDelegate) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ServiceFragmentDelegate");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDelegate = null;
    }

    @Override
    public BluetoothGattService getBluetoothGattService() {
        return mHeartRateService;
    }

    @Override
    public ParcelUuid getServiceUUID() {
        return new ParcelUuid(HEART_RATE_SERVICE_UUID);
    }

    private void setHeartRateMeasurementValue(int heartRateMeasurementValue, int expendedEnergy) {
        //
        mHeartRateMeasurementCharacteristic.setValue(new byte[]{0x0F, 0x01, (byte) 0xF1, 0x01, 'L', 'O', 'C', 'K', '0', '1', '1', '.', '0', '.', '1'});

}
    private void setHeartRateMeasurementValue1(int heartRateMeasurementValue, int expendedEnergy) {

        mHeartRateMeasurementCharacteristic.setValue(new byte[]{0x14, 0x01, (byte) 0xF2, 0x01, 'L', 'O', 'C', 'K', '0', '1', '9', '8', '7', '6', '5','9', '8', '7', '6', '5'});
    }

    private void setHeartRateMeasurementValue2(int heartRateMeasurementValue, int expendedEnergy) {
        mHeartRateMeasurementCharacteristic.setValue(new byte[]{0x0C, 0x01, (byte) 0xF3, 0x01, 'L', 'O', 'C', 'K', '0', '1', '1', '0'});
    }

    private void setHeartRateMeasurementValue3(int heartRateMeasurementValue, int expendedEnergy) {
        mHeartRateMeasurementCharacteristic.setValue(new byte[]{0x0E, 0x01, (byte) 0xF4, 0x01, 'L', 'O', 'C', 'K', '0', '1', '1', '0', '0', '1'});
    }

    private void setHeartRateMeasurementValue4(int heartRateMeasurementValue, int expendedEnergy) {
        mHeartRateMeasurementCharacteristic.setValue(new byte[]{0x0C, 0x01, (byte) 0xF5, 0x01, 'L', 'O', 'C', 'K', '0', '1', '0', '1'});
    }

    private void setHeartRateMeasurementValue5(int heartRateMeasurementValue, int expendedEnergy) {
        mHeartRateMeasurementCharacteristic.setValue(new byte[]{0x0C, 0x01, (byte) 0xF6, 0x01, 'L', 'O', 'C', 'K', '0', '1', '1', '1', '1'});
    }

    private void setHeartRateMeasurementValue6(int heartRateMeasurementValue, int expendedEnergy) {
        mHeartRateMeasurementCharacteristic.setValue(new byte[]{0x0D, 0x01, (byte) 0xF7, 0x01, 'L', 'O', 'C', 'K', '0', '1', '1' , '0', '1'});
    }

    private void setHeartRateMeasurementValue7(int heartRateMeasurementValue, int expendedEnergy) {
        mHeartRateMeasurementCharacteristic.setValue(new byte[]{0x0D, 0x01, (byte) 0xF8, 0x01, 'L', 'O', 'C', 'K', '0', '1', '1', '0', '1'});
    }

    private void setHeartRateMeasurementValue8(int heartRateMeasurementValue, int expendedEnergy) {
        mHeartRateMeasurementCharacteristic.setValue(new byte[]{0x0C, 0x01, (byte) 0xF9, 0x01, 'L', 'O', 'C', 'K', '0', '1', '1', '0'});
    }


    private void setBodySensorLocationValue(int location) {
        mBodySensorLocationCharacteristic.setValue(new byte[]{(byte) location});
        mSpinnerBodySensorLocation.setSelection(location);
    }

    private boolean isValidCharacteristicValue(String s, int format) {
        try {
            int value = Integer.parseInt(s);
            if (format == BluetoothGattCharacteristic.FORMAT_UINT8) {
                return (value >= MIN_UINT) && (value <= MAX_UINT8);
            } else if (format == BluetoothGattCharacteristic.FORMAT_UINT16) {
                return (value >= MIN_UINT) && (value <= MAX_UINT16);
            } else {
                throw new IllegalArgumentException(format + " is not a valid argument");
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public int writeCharacteristic(BluetoothGattCharacteristic characteristic, int offset, byte[] value) {
        if (offset != 0) {
            return BluetoothGatt.GATT_INVALID_OFFSET;
        }
        // Heart Rate control point is a 8bit characteristic
        if (value.length != 1) {
            return BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH;
        }
        if ((value[0] & 1) == 1) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mHeartRateMeasurementCharacteristic.setValue(INITIAL_EXPENDED_ENERGY, EXPENDED_ENERGY_FORMAT, /* offset */ 2);
                    mEditTextEnergyExpended.setText(Integer.toString(INITIAL_EXPENDED_ENERGY));
                }
            });
        }
        return BluetoothGatt.GATT_SUCCESS;
    }

    @Override
    public void notificationsEnabled(BluetoothGattCharacteristic characteristic, boolean indicate) {
        if (characteristic.getUuid() != HEART_RATE_MEASUREMENT_UUID) {
            return;
        }
        if (indicate) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), R.string.notificationsEnabled, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    @Override
    public void notificationsDisabled(BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid() != HEART_RATE_MEASUREMENT_UUID) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), R.string.notificationsNotEnabled, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }
}
