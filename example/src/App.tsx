import * as React from 'react';

import {
  StyleSheet,
  View,
  Text,
  type EmitterSubscription,
  DeviceEventEmitter,
  Platform,
  Button,
} from 'react-native';
import SmsRetriever from 'react-native-android-sms-retriever-api';

export default function App() {
  const [phoneNumber, setPhoneNumber] = React.useState<string | undefined>();
  const [result, setResult] = React.useState<string | undefined>();

  const requestPhoneNumber = async () => {
    // get list of available phone numbers
    try {
      const selectedPhone = await SmsRetriever.requestPhoneNumber();
      console.log('Selected Phone is : ' + selectedPhone);
      setPhoneNumber(selectedPhone);
    } catch (e) {
      console.warn('Get Phone error', e);
    }
  };

  React.useEffect(() => {
    async function getAppHashAsync() {
      // get App Hash
      const hash = await SmsRetriever.getAppHash();
      console.log('Your App Hash is : ' + hash);
    }
    // only to be used with Android
    if (Platform.OS === 'android') getAppHashAsync();
  }, []);

  React.useEffect(() => {
    let smsListener: undefined | EmitterSubscription;
    async function smsListenAsync() {
      try {
        // set Up SMS Listener
        smsListener = DeviceEventEmitter.addListener(
          SmsRetriever.SMS_EVENT,
          (data: any) => {
            setResult(JSON.stringify(data));
          }
        );
        // start listening sms retriever
        await SmsRetriever.startSmsRetriever();
      } catch (e) {
        console.warn('sms retriever error', e);
      }
    }
    // only to be used with Android
    if (Platform.OS === 'android') smsListenAsync();
    return () => {
      // remove the listener on unmount
      smsListener?.remove();
    };
  }, []);

  return (
    <View style={styles.container}>
      <Button title="Request phone number" onPress={requestPhoneNumber} />
      {phoneNumber && (
        <Text style={styles.box}>Phone Number: {phoneNumber}</Text>
      )}
      <Text style={styles.box}>SMS Result: {result}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    marginVertical: 16,
  },
});
