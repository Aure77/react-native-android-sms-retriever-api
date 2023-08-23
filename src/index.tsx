import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-android-sms-retriever' doesn't seem to be linked. Make sure: \n\n` +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

type AndroidSmsRetrieverType = {
  requestPhoneNumber(): Promise<string>;
  startSmsRetriever(): Promise<boolean>;
  getAppHash(): Promise<string>;
  SMS_EVENT: string;
};

const AndroidSmsRetriever: AndroidSmsRetrieverType =
  NativeModules.AndroidSmsRetriever
    ? {
        ...NativeModules.AndroidSmsRetriever,
      }
    : new Proxy(
        {},
        {
          get() {
            if (Platform.OS === 'android') {
              throw new Error(LINKING_ERROR);
            }
            return null;
          },
        }
      );

export default AndroidSmsRetriever;
