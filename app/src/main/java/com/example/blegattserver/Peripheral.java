//Yehiya 27.09.19
package com.example.blegattserver;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import com.example.blegattserver.ServiceFragment.ServiceFragmentDelegate;

public  class Peripheral extends Activity implements ServiceFragmentDelegate {
  private HashSet<BluetoothDevice> mBluetoothDevices;
  private BluetoothGattService mBluetoothGattService;
  private static final int REQUEST_ENABLE_BT = 1;
  private static final String TAG = Peripheral.class.getCanonicalName();
  private BluetoothAdapter mBluetoothAdapter;
  private TextView mTVDeviceId;
  public EditText mEditText;
  private Button mButtonsend;
  private TextView mConnectionStatus;
  private Button mButtonclear;
  private Button mButtonStartThread;
  private Button mButtonStopThread;
  private ListView mListView;
  private ListAdapter mListAdapter;
  private List<PrintText> mStringList;
  private String mText;
  private String androidDeviceId;
  private boolean m_Send = true;
  private AdvertiseData mAdvData;
  private AdvertiseData mAdvScanResponse;
  private AdvertiseSettings mAdvSettings;
  private BluetoothLeAdvertiser mAdvertiser;
  private static final String CURRENT_FRAGMENT_TAG = "CURRENT_FRAGMENT";
  //UUID's
  private static final UUID CHARACTERISTIC_USER_DESCRIPTION_UUID = UUID.fromString("00002901-0000-1000-8000-00805f9b34fb");
  private static final UUID CLIENT_CHARACTERISTIC_CONFIGURATION_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
  private static final UUID UUID_SERVER = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
  private static final UUID UUID_CHARREAD = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
  private static final UUID UUID_CHARWRITE = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
  private static final UUID UUID_DESCRIPTOR = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");
  private BluetoothManager mBluetoothManager;
  private BluetoothGattServer bluetoothGattServer;
  private BluetoothGattCharacteristic characteristicRead;
  private BluetoothDevice mBluetoothDevice;
  private final Handler handler = new Handler();
  private Runnable runnable;
  private TextView mAdvStatus;
  private ServiceFragment mCurrentServiceFragment;

  private final AdvertiseCallback mAdvCallback = new AdvertiseCallback() {
    @Override
    public void onStartFailure(int errorCode) {
      super.onStartFailure(errorCode);
      Log.e(TAG, "Not broadcasting: " + errorCode);
      int statusText;
      switch (errorCode) {
        case ADVERTISE_FAILED_ALREADY_STARTED:
          statusText = R.string.status_advertising;
          Log.w(TAG, "App was already advertising");
          break;
        case ADVERTISE_FAILED_DATA_TOO_LARGE:
          statusText = R.string.status_advDataTooLarge;
          break;
        case ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
          statusText = R.string.status_advFeatureUnsupported;
          break;
        case ADVERTISE_FAILED_INTERNAL_ERROR:
          statusText = R.string.status_advInternalError;
          break;
        case ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
          statusText = R.string.status_advTooManyAdvertisers;
          break;
        default:
          statusText = R.string.status_notAdvertising;
          Log.wtf(TAG, "Unhandled error: " + errorCode);
      }
      mAdvStatus.setText(statusText);
    }
  };
  /////////////////////////////////
  ////// Lifecycle Callbacks //////
  /////////////////////////////////

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    mEditText = (EditText) findViewById(R.id.et_text);
    mButtonsend = (Button) findViewById(R.id.bt_send);
    mButtonclear = (Button) findViewById(R.id.bt_clear);
    mListView = (ListView) findViewById(R.id.listView);
    mStringList = new ArrayList<>();
    mButtonsend.setOnClickListener(btListner);
    mButtonclear.setOnClickListener(btListner);
    mBluetoothDevices = new HashSet<>();
    mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
    mBluetoothAdapter = mBluetoothManager.getAdapter();


    //1. Get the BluetoothAdapter
    // Use this check to determine whether BLE is supported on the device.  Then
    // you can selectively disable BLE-related features.
    if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
      Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
      finish();


    }

    // Initializes Bluetooth adapter.
    mBluetoothManager =
            (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
    mBluetoothAdapter = mBluetoothManager.getAdapter();

    //2.Enable Bluetooth
    // Ensures Bluetooth is available on the device and it is enabled.  If not,
    // displays a dialog requesting user permission to enable Bluetooth.
    if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    } else {
      mStringList.add(new PrintText("Starting Server...."));
      addToList(mStringList);

      // If we are not being restored from a previous state then create and add the fragment.
      if (savedInstanceState == null) {
        int peripheralIndex = getIntent().getIntExtra(MainActivity.EXTRA_PERIPHERAL_INDEX,
                /* default */ -1);
        if (peripheralIndex == 0) {

          //getClientCharacteristicConfigurationDescriptor();
          mCurrentServiceFragment = new Heartratefragment();

        } else {
          Log.wtf(TAG, "Service doesn't exist");
        }
        getFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, mCurrentServiceFragment, CURRENT_FRAGMENT_TAG)
                .commit();
      } else {
        mCurrentServiceFragment = (ServiceFragment) getFragmentManager()
                .findFragmentByTag(CURRENT_FRAGMENT_TAG);
      }
      mBluetoothGattService = mCurrentServiceFragment.getBluetoothGattService();
      initGATTServer();
    }

  }

  //Advertise
  private void initGATTServer() {
    AdvertiseSettings settings = new AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(true)
            .build();

    AdvertiseData advertiseData = new AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .setIncludeTxPowerLevel(true)
           // .addServiceUuid(mCurrentServiceFragment.getServiceUUID())
            .build();

    AdvertiseData scanResponseData = new AdvertiseData.Builder()
            .setIncludeTxPowerLevel(true)
            .setIncludeDeviceName(true)
            .build();
    AdvertiseCallback callback = new AdvertiseCallback() {

      @RequiresApi(api = Build.VERSION_CODES.M)
      @Override
      public void onStartSuccess(AdvertiseSettings settingsInEffect) {
        Log.d(TAG, "BLE advertisement added successfully");
        mStringList.add(new PrintText("GATTServer Success"));
        addToList(mStringList);
        initServices(getContext());
      }

      @RequiresApi(api = Build.VERSION_CODES.M)
      @Override
      public void onStartFailure(int errorCode) {
        Log.e(TAG, "Failed to add BLE advertisement, reason: " + errorCode);
        mStringList.add(new PrintText("GATTServer Failure"));
        addToList(mStringList);
      }
    };
    BluetoothLeAdvertiser bluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
    bluetoothLeAdvertiser.startAdvertising(settings, advertiseData, scanResponseData, callback);
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  private void initServices(Context context) {
    bluetoothGattServer = mBluetoothManager.openGattServer(context, bluetoothGattServerCallback);
    BluetoothGattService service = new BluetoothGattService(UUID_SERVER, BluetoothGattService.SERVICE_TYPE_PRIMARY);
    //add a read characteristic.
    characteristicRead = new BluetoothGattCharacteristic(UUID_CHARREAD, BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ);
    //add a descriptor
    BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(UUID_DESCRIPTOR, BluetoothGattCharacteristic.PERMISSION_WRITE);
    characteristicRead.addDescriptor(descriptor);
    service.addCharacteristic(characteristicRead);
    BluetoothGattCharacteristic characteristicWrite = new BluetoothGattCharacteristic(UUID_CHARWRITE, BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_WRITE);
    service.addCharacteristic(characteristicWrite);
    BluetoothGattCharacteristic characteristicNotification = new BluetoothGattCharacteristic(UUID_DESCRIPTOR, BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_READ);
    service.addCharacteristic(characteristicNotification);
    bluetoothGattServer.addService(service);
    mStringList.add(new PrintText("Services OK"));
    addToList(mStringList);
    Log.e(TAG, "2. initServices ok");
  }

  /**
   * Callback for service events
   */
  private BluetoothGattServerCallback bluetoothGattServerCallback = new BluetoothGattServerCallback() {

    /**
     * 1.When the connection status changes
     * @param device
     * @param status
     * @param newState
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onConnectionStateChange(final BluetoothDevice device, int status, final int newState) {
      Log.e(TAG, String.format("1.onConnectionStateChange：device name = %s, address = %s", device.getName(), device.getAddress()));
      Log.e(TAG, String.format("1.onConnectionStateChange：status = %s, newState =%s ", status, newState));
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (newState == 2) {

            mStringList.add(new PrintText("Device Address : " + device.getAddress() + " is Connected"));
            mBluetoothDevice = device;

          } else {
            mStringList.add(new PrintText("Device Address : " + device.getAddress() + " is Disconnected"));
          }
          mListAdapter = new ListAdapter(getApplicationContext(), mStringList);
          mListView.setAdapter(mListAdapter);
        }
      });
      super.onConnectionStateChange(device, status, newState);
      if (status == BluetoothGatt.GATT_SUCCESS) {
        if (newState == BluetoothGatt.STATE_CONNECTED) {
          mBluetoothDevices.add(device);
          updateConnectedDevicesStatus();
          Log.v(TAG, "Connected to device: " + device.getAddress());
        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
          mBluetoothDevices.remove(device);
          updateConnectedDevicesStatus();
          Log.v(TAG, "Disconnected from device");
        }
      } else {
        mBluetoothDevices.remove(device);
        updateConnectedDevicesStatus();
        // There are too many gatt errors (some of them not even in the documentation) so we just
        // show the error to the user.
        final String errorMessage = getString(R.string.status_errorWhenConnecting) + ": " + status;
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(Peripheral.this, errorMessage, Toast.LENGTH_LONG).show();
          }
        });
        Log.e(TAG, "Error when connecting: " + status);
      }

    }

    @Override
    public void onServiceAdded(int status, BluetoothGattService service) {
      super.onServiceAdded(status, service);
      Log.e(TAG, String.format("onServiceAdded：status = %s", status));
    }

    @Override
    public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
      super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
     // Log.d(TAG, "Device tried to read characteristic: " + characteristic.getUuid());
      Log.d(TAG, "Value: " + Arrays.toString(characteristic.getValue()));
      if (offset != 0) {
        bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_INVALID_OFFSET, offset,/* value (optional) */ null);
        return;
      }
      bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
    }
    /**
     * 3. onCharacteristicWriteRequest,接收具体的字节
     *
     //*
     */
    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                             BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
                                             int offset, byte[] value) {
      super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
      Log.v(TAG, "Characteristic Write request: " + Arrays.toString(value));
      int status = mCurrentServiceFragment.writeCharacteristic(characteristic, offset, value);
      if (responseNeeded) {
        bluetoothGattServer.sendResponse(device, requestId, status,/* No need to respond with an offset */ 0,/* No need to respond with a value */ null);
      }
      onResponseToClient(value, device, requestId, characteristic);
    }
    /**
     * 2.When the description is written, it is executed here  bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS...  收，触发 onCharacteristicWriteRequest
     * @param device
     * @param requestId
     * @param descriptor
     * @param preparedWrite
     * @param responseNeeded
     * @param offset
     * @param value
     */
    @Override
    public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
      super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
  //    Log.v(TAG, "Descriptor Write Request " + descriptor.getUuid() + " " + Arrays.toString(value));
      int status = BluetoothGatt.GATT_SUCCESS;
      if (descriptor.getUuid() == CLIENT_CHARACTERISTIC_CONFIGURATION_UUID) {
        BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
        boolean supportsNotifications = (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
        boolean supportsIndications = (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0;
        if (!(supportsNotifications || supportsIndications)) {
          status = BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED;
        } else if (value.length != 2) {
          status = BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH;
        } else if (Arrays.equals(value, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)) {
          status = BluetoothGatt.GATT_SUCCESS;
          mCurrentServiceFragment.notificationsDisabled(characteristic);
          descriptor.setValue(value);
        } else if (supportsNotifications && Arrays.equals(value, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
          status = BluetoothGatt.GATT_SUCCESS;
          mCurrentServiceFragment.notificationsEnabled(characteristic, false /* indicate */);
          descriptor.setValue(value);
        } else if (supportsIndications && Arrays.equals(value, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)) {
          status = BluetoothGatt.GATT_SUCCESS;
          mCurrentServiceFragment.notificationsEnabled(characteristic, true /* indicate */);
          descriptor.setValue(value);
        } else {
          status = BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED;
        }
      } else {
        status = BluetoothGatt.GATT_SUCCESS;
        descriptor.setValue(value);
      }
      if (responseNeeded) {
        bluetoothGattServer.sendResponse(device, requestId, status,/* No need to respond with offset */ 0,/* No need to respond with a value */ null);
      }
    }

    /**
     * 5
     Features are read. When the response is successful, the client will read and then trigger the method.
     * @param device
     * @param requestId
     * @param offset
     * @param descriptor
     */
    @Override
    public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
      super.onDescriptorReadRequest(device, requestId, offset, descriptor);
      Log.d(TAG, "Device tried to read descriptor: " + descriptor.getUuid());
      Log.d(TAG, "Value: " + Arrays.toString(descriptor.getValue()));
      if (offset != 0) {
        bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_INVALID_OFFSET, offset,/* value (optional) */ null);
        return;
      }
      bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, descriptor.getValue());
    }

    @Override
    public void onNotificationSent(BluetoothDevice device, int status) {
      super.onNotificationSent(device, status);
      Log.v(TAG, "Notification sent. Status: " + status);
    }

    @Override
    public void onMtuChanged(BluetoothDevice device, int mtu) {
      super.onMtuChanged(device, mtu);
      Log.e(TAG, String.format("onMtuChanged：mtu = %s", mtu));
    }

    @Override
    public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
      super.onExecuteWrite(device, requestId, execute);
      Log.e(TAG, String.format("onExecuteWrite：requestId = %s", requestId));
    }

  };
  /**
   * 4.
   * Processing response content
   */
  @RequiresApi(api = Build.VERSION_CODES.O)
  private void onResponseToClient(byte[] reqeustBytes, BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic) {
    Log.e(TAG, String.format("4.onResponseToClient：device name = %s, address = %s", device.getName(), device.getAddress()));
    Log.e(TAG, String.format("4.onResponseToClient：requestId = %s", requestId));
    Log.d(TAG, "onResponseToClient: reqeustBytes : " + reqeustBytes);

  //  String s=new BigInteger(1, someByteArray).toString(16);




    String msg = OutputStringUtil.transferForPrint(reqeustBytes);

   // Charset.forName("ISO-8859-1").decode(ByteBuffer.wrap(reqeustBytes));

  //  ByteArrayInputStream bis = new ByteArrayInputStream(reqeustBytes.g("ISO-8859-1").getBytes("ISO-8859-1"));

    Log.d(TAG, "onResponseToClient: msg : " + msg);
    Log.d(TAG, "onResponseToClient: msg : " + msg);
    println("4.response:" + msg);

    //Charset.forName("ISO-8859-1").decode(bufferA);

    final String str = new String(reqeustBytes);




    mBluetoothDevice = device;

    runOnUiThread(new Runnable() {
      @RequiresApi(api = Build.VERSION_CODES.M)
      @Override
      public void run() {
        mStringList.add(new PrintText("Response : " + str));
        mListAdapter = new ListAdapter(getApplicationContext(), mStringList);
        mListView.setAdapter(mListAdapter);
      }
    });
  }

  View.OnClickListener btListner = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      int id = v.getId();
      if (id == R.id.bt_send) {
        characteristicRead.setValue(mEditText.getText().toString().trim());
        bluetoothGattServer.notifyCharacteristicChanged(mBluetoothDevice, characteristicRead, false);
      } else if (id == R.id.bt_clear) {
        mStringList.clear();
        mListAdapter.notifyDataSetChanged();
      }

    }
  };

  private void println(String s) {
    Log.e(TAG, s);
  }

  public Context getContext() {
    return this;
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  private void addToList(List<PrintText> mList) {
    mListAdapter = new ListAdapter(this, mList);
    mListView.setAdapter(mListAdapter);
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
// TODO Auto-generated method stub
    if (requestCode == REQUEST_ENABLE_BT) {
      if (resultCode == RESULT_OK) {
        Toast.makeText(getApplicationContext(), "BlueTooth is now Enabled Please Re_enter again!!!", Toast.LENGTH_LONG).show();
        mStringList.add(new PrintText("Starting Server...."));
        addToList(mStringList);
        initGATTServer();
       // onStart();
      }
      if (resultCode == RESULT_CANCELED) {
        Toast.makeText(getApplicationContext(), "Please go back and re_enter ", Toast.LENGTH_LONG).show();

      }
    }
  }

  @Override
  protected void onStart() {
    super.onStart();

   // resetStatusViews();
    // If the user disabled Bluetooth when the app was in the background,
    // openGattServer() will return null.
    bluetoothGattServer = mBluetoothManager.openGattServer(this, bluetoothGattServerCallback);
    if (bluetoothGattServer == null) {
      ensureBleFeaturesAvailable();
      return;
    }
    // Add a service for a total of three services (Generic Attribute and Generic Access
    // are present by default).
//    bluetoothGattServer.addService(mBluetoothGattService);

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_disconnect_devices) {
      disconnectFromDevices();
      return true /* event_consumed */;
    }
    return false /* event_consumed */;
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (bluetoothGattServer != null) {
      bluetoothGattServer.close();
    }
    if (mBluetoothAdapter.isEnabled() && mAdvertiser != null) {
      // If stopAdvertising() gets called before close() a null
      // pointer exception is raised.
      mAdvertiser.stopAdvertising(mAdvCallback);
    }
  }

  private void updateConnectedDevicesStatus() {
    final String message = getString(R.string.status_devicesConnected) + " "
            + mBluetoothManager.getConnectedDevices(BluetoothGattServer.GATT).size();

  }

  private void ensureBleFeaturesAvailable() {
    if (mBluetoothAdapter == null) {
      Toast.makeText(this, R.string.bluetoothNotSupported, Toast.LENGTH_LONG).show();
      Log.e(TAG, "Bluetooth not supported");
      finish();
    } else if (!mBluetoothAdapter.isEnabled()) {
      // Make sure bluetooth is enabled.
      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }
  }

  private void disconnectFromDevices() {
    Log.d(TAG, "Disconnecting devices...");
    for (BluetoothDevice device : mBluetoothManager.getConnectedDevices(
            BluetoothGattServer.GATT)) {
      Log.d(TAG, "Devices: " + device.getAddress() + " " + device.getName());
      bluetoothGattServer.cancelConnection(device);
    }
  }

  ///////////////////////
  ////// Bluetooth //////
  ///////////////////////
  public static BluetoothGattDescriptor getClientCharacteristicConfigurationDescriptor() {
    BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION_UUID, (BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE));
    descriptor.setValue(new byte[]{0, 0});
    return descriptor;
  }

  public static BluetoothGattDescriptor getCharacteristicUserDescriptionDescriptor(String defaultValue) {
    BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(CHARACTERISTIC_USER_DESCRIPTION_UUID, (BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE));
    try {
      descriptor.setValue(defaultValue.getBytes("UTF-8"));
    } finally {
      return descriptor;
    }
  }


  public void sendNotificationToDevices(BluetoothGattCharacteristic characteristic) {
    boolean indicate = (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) == BluetoothGattCharacteristic.PROPERTY_INDICATE;
    for (BluetoothDevice device : mBluetoothDevices) {
      bluetoothGattServer.notifyCharacteristicChanged(device, characteristic, indicate);

    }
  }
  @Override
  public void sendNotificationToDevices(BluetoothDevice device, int status) {
  }
  @Override
  public void sendNotificationToDevices(BluetoothDevice device, int status, BluetoothGattCharacteristic characteristic) {
  }
}