# react-native-android-sms-retriever

React Native implementation of [Android SMS Retriever API](https://developers.google.com/identity/sms-retriever/overview). No READ_SMS permission is required.

For bare react-native projects only. Not applicable for expo projects.

For better understanding, please refer this [article](https://medium.com/android-dev-hacks/autofill-otp-verification-with-latest-sms-retriever-api-73c788636783).

## Installation

```sh
yarn add react-native-android-sms-retriever
```

## Usage

```js
import SmsRetriever from 'react-native-android-sms-retriever';

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
        // start Retriever;
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
      <Text style={styles.box}>Result: {result}</Text>
    </View>
  );
}
```

## Get App Hash string

Each build type (debug, release) can use a different signing config, so your hash can differ depending on your sign key & applicationId.

[Follow these steps](https://developers.google.com/identity/sms-retriever/verify#computing_your_apps_hash_string) to generate your app hash string for each variant

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
