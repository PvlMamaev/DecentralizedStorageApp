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
    (window as any).sendCid = async (bocBase64: string) => {
      const tx = {
        validUntil: Math.floor(Date.now() / 1000) + 60,
        messages: [{
          address: 'EQANpQ6yztUW2Cl9EbSFIKgzcdacgv6MOFfR4qpZgs5YOuMN',
          amount: '50000000',
          payload: bocBase64
        }]
      };
      try {
        const res = await tonConnectUI.sendTransaction(tx);
        (window as any).AndroidBridge?.onTxResult(JSON.stringify(res));
      } catch (e: any) {
        (window as any).AndroidBridge?.onTxError(e.message);
      }
    };
  }, [tonConnectUI]);  

 async function sendTx() {
    const tx = {
      validUntil: Math.floor(Date.now() / 1000) + 60,
      messages: [{
        address: 'EQANpQ6yztUW2Cl9EbSFIKgzcdacgv6MOFfR4qpZgs5YOuMN',
        amount: '50000000',
        payload: 'BASE64_BOC'
      }]
    };

    try {
      const res = await tonConnectUI.sendTransaction(tx);
      (window as any).AndroidBridge?.onTxResult(JSON.stringify(res));
    } catch (e: any) {
      (window as any).AndroidBridge?.onTxError(e.message);
    }
  }

  return (
    <>
      <TonConnectButton />
      <button onClick={sendTx}>Send transaction</button>
    </>
  );
}

export default function App() {
  return (
    <TonConnectUIProvider
      manifestUrl={MANIFEST}
      actionsConfiguration={{ twaReturnUrl: 'myapp://tc-return' }}
    >
      <Page />
    </TonConnectUIProvider>
  );
}

