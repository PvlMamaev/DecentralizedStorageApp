import {
  TonConnectUIProvider,
  TonConnectButton,
  useTonConnectUI
} from '@tonconnect/ui-react';

function Page() {
  const [tonConnectUI] = useTonConnectUI();

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
      // передадим результат в Android
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
      manifestUrl="tonconnect-manifest.json"
      actionsConfiguration={{ twaReturnUrl: 'myapp://tc-return' }}
    >
      <Page />
    </TonConnectUIProvider>
  );
}

