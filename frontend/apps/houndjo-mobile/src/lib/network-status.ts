import NetInfo from '@react-native-community/netinfo';
import { onlineManager } from '@tanstack/react-query';

let listenerInstalled = false;

/**
 * Wires TanStack Query's online/offline state to the device's actual
 * connectivity so queries pause while offline (rather than failing and
 * retrying into a wall) and automatically resume once connectivity returns.
 */
export function setupNetworkStatusListener() {
  if (listenerInstalled) return;
  listenerInstalled = true;

  onlineManager.setEventListener((setOnline) => {
    return NetInfo.addEventListener((state) => {
      setOnline(state.isConnected === true && state.isInternetReachable !== false);
    });
  });
}
