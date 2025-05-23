import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

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

export default TurboModuleRegistry.getEnforcing<Spec>('AndroidSmsRetriever');
