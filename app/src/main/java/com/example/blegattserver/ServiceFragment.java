//Yehiya 27.09.19
package com.example.blegattserver;

import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.ParcelUuid;

public abstract class ServiceFragment extends Fragment {
  public abstract BluetoothGattService getBluetoothGattService();
  public abstract ParcelUuid getServiceUUID();
  public int writeCharacteristic(BluetoothGattCharacteristic characteristic, int offset, byte[] value) {
      throw new UnsupportedOperationException("Method writeCharacteristic not overridden");
  };

  /**
   * Function to notify to the ServiceFragment that a device has disabled notifications on a
   * CCC descriptor.
   *
   * The ServiceFragment should update the UI to reflect the change.
   * @param characteristic Characteristic written to
   */
  public void notificationsDisabled(BluetoothGattCharacteristic characteristic) {
    throw new UnsupportedOperationException("Method notificationsDisabled not overridden");
  };

  /**
   * Function to notify to the ServiceFragment that a device has enabled notifications on a
   * CCC descriptor.
   *
   * The ServiceFragment should update the UI to reflect the change.
   * @param characteristic Characteristic written to
   * @param indicate Boolean that says if it's indicate or notify.
   */
  public void notificationsEnabled(BluetoothGattCharacteristic characteristic, boolean indicate) {
    throw new UnsupportedOperationException("Method notificationsEnabled not overridden");
  };

  /**
   * This interface must be implemented by activities that contain a ServiceFragment to allow an
   * interaction in the fragment to be communicated to the activity.
   */
  public interface ServiceFragmentDelegate {
    void sendNotificationToDevices(BluetoothGattCharacteristic characteristic);

      void sendNotificationToDevices(BluetoothDevice device, int status);

      void sendNotificationToDevices(BluetoothDevice device, int status, BluetoothGattCharacteristic characteristic);
  }


}


