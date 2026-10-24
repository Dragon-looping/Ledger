import puppeteer from 'puppeteer-core';
import path from 'path';

const EDGE_PATH = 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe';
const ARTIFACT_DIR = 'C:\\Users\\Hp\\.gemini\\antigravity\\brain\\2a65b030-486d-446b-8254-33aafefff5c2';

async function capture() {
  console.log('Launching Edge browser...');
  const browser = await puppeteer.launch({
    executablePath: EDGE_PATH,
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox', '--disable-gpu']
  });

  const page = await browser.newPage();

  // 1. Desktop Screenshot (1440x900)
  console.log('Capturing Desktop View (1440x900)...');
  await page.setViewport({ width: 1440, height: 900, deviceScaleFactor: 2 });
  await page.goto('http://localhost:5173', { waitUntil: 'networkidle0', timeout: 15000 });
  await new Promise((r) => setTimeout(r, 1500)); // wait for motion transitions

  const desktopPath = path.join(ARTIFACT_DIR, 'ledger-desktop.png');
  await page.screenshot({ path: desktopPath, fullPage: false });
  console.log(`Saved: ${desktopPath}`);

  // 2. Mobile Screenshot (390x844 iPhone 14/15 width)
  console.log('Capturing Mobile View (390x844)...');
  await page.setViewport({ width: 390, height: 844, deviceScaleFactor: 2, isMobile: true, hasTouch: true });
  await page.goto('http://localhost:5173', { waitUntil: 'networkidle0', timeout: 15000 });
  await new Promise((r) => setTimeout(r, 1500));

  const mobilePath = path.join(ARTIFACT_DIR, 'ledger-mobile.png');
  await page.screenshot({ path: mobilePath, fullPage: false });
  console.log(`Saved: ${mobilePath}`);

  // 3. Transactions route on Desktop
  console.log('Capturing Journal / Transactions View...');
  await page.setViewport({ width: 1440, height: 900, deviceScaleFactor: 2 });
  await page.goto('http://localhost:5173/transactions', { waitUntil: 'networkidle0', timeout: 15000 });
  await new Promise((r) => setTimeout(r, 1500));
  const txPath = path.join(ARTIFACT_DIR, 'ledger-transactions.png');
  await page.screenshot({ path: txPath, fullPage: false });
  console.log(`Saved: ${txPath}`);

  // 4. Budgets route on Desktop
  console.log('Capturing Budgets View...');
  await page.goto('http://localhost:5173/budgets', { waitUntil: 'networkidle0', timeout: 15000 });
  await new Promise((r) => setTimeout(r, 1500));
  const budgetsPath = path.join(ARTIFACT_DIR, 'ledger-budgets.png');
  await page.screenshot({ path: budgetsPath, fullPage: false });
  console.log(`Saved: ${budgetsPath}`);

  await browser.close();
  console.log('All screenshots captured successfully!');
}

capture().catch((err) => {
  console.error('Screenshot capture failed:', err);
  process.exit(1);
});
