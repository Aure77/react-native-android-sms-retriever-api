# react-native-android-sms-retriever-api

React Native implementation of [Android SMS Retriever API](https://developers.google.com/identity/sms-retriever/overview). No READ_SMS permission is required.

For bare react-native projects only. Not applicable for expo projects.

For better understanding, please refer this [article](https://medium.com/android-dev-hacks/autofill-otp-verification-with-latest-sms-retriever-api-73c788636783).

## Installation

```sh
yarn add react-native-android-sms-retriever-api
```

## Usage

Which API to use ? : https://developers.google.com/identity/sms-retriever/choose-an-api

### SMS Retriever

See https://developers.google.com/identity/sms-retriever/overview

```js
import SmsRetriever from 'react-native-android-sms-retriever-api';

export default function App() {
  const [result, setResult] = React.useState<string | undefined>();

  React.useEffect(() => {
    let smsListener: undefined | EmitterSubscription;
    async function smsListenAsync() {
      try {
        // set Up SMS Listener
        smsListener = DeviceEventEmitter.addListener(
          SmsRetriever.SMS_EVENT,
          (event) => {
            console.log("sms_event", event);
            if ("message" in event) {
              // sub?.remove(); // optional for processing once
              const smsOtp = parseOneTimeCode(event.message);
              if (smsOtp) {
                setOtp(smsOtp);
              } else {
                console.warn("No OTP found in SMS");
              }
            } else {
              // handle timeout / error
            }
          }
        );
        // start sms retriever
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
      <Text style={styles.box}>Result: {result}</Text>
    </View>
  );
}

const parseOneTimeCode = (message?: string) => {
  if (!message) {
    return null;
  }
  const matcher = message.match(/your code is (\w+)/im);
  return matcher?.[1] ?? null;
};
```

### SMS User Consent

See https://developers.google.com/identity/sms-retriever/user-consent/overview

<img src="https://github.com/Aure77/react-native-android-sms-retriever-api/assets/1374354/f657f237-85da-4e9e-92b8-feb9b8c7f611" width="360" />

```js
import SmsRetriever from 'react-native-android-sms-retriever-api';

export default function App() {
  const [result, setResult] = React.useState<string | undefined>();

  React.useEffect(() => {
    let smsListener: undefined | EmitterSubscription;
    async function smsListenAsync() {
      try {
        // set Up SMS Listener
        smsListener = DeviceEventEmitter.addListener(
          SmsRetriever.SMS_EVENT,
          (event) => {
            // handle event / parse OTP as you want
            setResult(JSON.stringify(event));
          }
        );
        // start sms user consent
        await SmsRetriever.startSmsUserConsent();
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
      <Text style={styles.box}>Result: {result}</Text>
    </View>
  );
}
```

### Request user phone number

```js
import SmsRetriever from 'react-native-android-sms-retriever-api';

export default function App() {
  const [phoneNumber, setPhoneNumber] = React.useState<string | undefined>();

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

  return (
    <View style={styles.container}>
      <Button title="Request phone number" onPress={requestPhoneNumber} />
      {phoneNumber && (
        <Text style={styles.box}>Phone Number: {phoneNumber}</Text>
      )}
    </View>
  );
}
```

### Get App Hash string

Each build type (debug, release) can use a different signing config, so your hash can differ depending on your sign key & applicationId.

[Follow these steps](https://developers.google.com/identity/sms-retriever/verify#computing_your_apps_hash_string) to generate your app hash string for each variant

or you can use helper at runtime (on each variant):

```js
import SmsRetriever from 'react-native-android-sms-retriever-api';

export default function App() {
  const [result, setResult] = React.useState<string | undefined>();

  React.useEffect(() => {
    async function getAppHashAsync() {
      // get App Hash
      const hash = await SmsRetriever.getAppHash();
      setResult(hash);
      console.log('Your App Hash is : ' + hash);
    }
    // only to be used with Android
    if (Platform.OS === 'android') getAppHashAsync();
  }, []);

  return (
    <View style={styles.container}>
      <Text style={styles.box}>Hash: {result}</Text>
    </View>
  );
}
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
