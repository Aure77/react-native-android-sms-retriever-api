import { Platform, TurboModuleRegistry, type TurboModule } from 'react-native';

export interface Spec extends TurboModule {
  requestPhoneNumber(): Promise<string>;
  startSmsRetriever(): Promise<boolean>;
  startSmsUserConsent(): Promise<boolean>;
  getAppHash(): Promise<string>;
  getSmsRetrieverCompatibility(): Promise<{
    isGooglePlayServicesAvailable: boolean;
    hasGooglePlayServicesSupportedVersion: boolean;
  }>;
  getConstants(): {
    SMS_EVENT: string;
  };
}

const NativeSmsRetrieverModule: Spec | undefined =
  Platform.OS === 'android'
    ? TurboModuleRegistry.getEnforcing<Spec>('AndroidSmsRetriever')
    : undefined;

export default NativeSmsRetrieverModule;
