import { useEffect } from 'react';
import {
  TonConnectUIProvider,
  TonConnectButton,
  useTonConnectUI
} from '@tonconnect/ui-react';


const MANIFEST = 'https://pvlmamaev.github.io/DecentralizedStorageApp/wallet-ui/public/tonconnect-manifest.json';

function Page() {
  const [tonConnectUI] = useTonConnectUI();

  useEffect(() => {
// Функция отправки транзакции для kotlin
    (window as any).sendCid = async (bocBase64: string) => {
// Здесь мы создаем переменную вида json чтобы создать транзакцию
      const tx = {
        validUntil: Math.floor(Date.now() / 1000) + 60,
        messages: [{
          address: 'EQANpQ6yztUW2Cl9EbSFIKgzcdacgv6MOFfR4qpZgs5YOuMN',
          amount: '50000000',
          payload: bocBase64
        }]
      };
// Здесь мы вызываем функцию отправки транзакции из библиотеки
      try {
        const res = await tonConnectUI.sendTransaction(tx);
        (window as any).AndroidBridge?.onTxResult(JSON.stringify(res));
      } catch (e: any) {
        (window as any).AndroidBridge?.onTxError(e.message);
      }
    };

// Функция проверки подключения кошелька для kotlin
    (window as any).checkConnection = () => {
      // SDK хранит текущее подключение в tonConnectUI.connector.wallet
      const isConnected = !!(tonConnectUI.connector.wallet)
      return isConnected
    }

  }, [tonConnectUI]);  
 
return (
    <>
      <TonConnectButton />
    </>
  );
}

export default function App() {
  return (
    <TonConnectUIProvider
      manifestUrl={MANIFEST} 
    >
      <Page />
    </TonConnectUIProvider>
  );
}

