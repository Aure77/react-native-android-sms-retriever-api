import { Platform } from 'react-native';
import NativeAndroidSmsRetriever, {
  type Spec,
} from './NativeAndroidSmsRetriever';

const LINKING_ERROR =
  `The package 'react-native-android-sms-retriever-api' doesn't seem to be linked. Make sure: \n\n` +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const AndroidSmsRetriever: Spec = NativeAndroidSmsRetriever
  ? NativeAndroidSmsRetriever
  : (new Proxy(
      {},
      {
        get() {
          if (Platform.OS === 'android') {
            throw new Error(LINKING_ERROR);
          }
          return null;
        },
      }
    ) as Spec);

export default AndroidSmsRetriever;
