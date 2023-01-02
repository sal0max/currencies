# APIs

There are a number of (free) currency rate APIs available. Most are designed basically the same,
so it should be possible to switch APIs without having to change the data model too much.

This app currently includes exchangerate.host, frankfurter.app and fer.ee.

| service                                                       | free api requests/month                                                                                                                                                                                                                                        | format | currencies         | updates                                        | data source                                                                                                                                                          |
|---------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|--------------------|------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml | unlimited                                                                                                                                                                                                                                                      | xml    | 33                 |                                                |                                                                                                                                                                      |
| https://www.imf.org/external/np/fin/data/rms_five.aspx        | unlimited                                                                                                                                                                                                                                                      | xls    | 39                 |                                                |                                                                                                                                                                      |
| **https://exchangerate.host**                                 | API requests made by a throttled user or app will fail. All API requests are subject to rate limits. Real time rate limit usage statistics are described in headers that are included with most API responses once enough calls have been made to an endpoint. | json   | ~170 + 6000 crypto | "updated on daily basis"                       | Currency data delivered are sourced from financial data providers and banks, including the European Central Bank.                                                    |
| **https://www.frankfurter.app**                               | unlimited                                                                                                                                                                                                                                                      | json   | 33                 | once/day                                       | European Central Bank                                                                                                                                                |
| **https://www.fer.ee**                                        | unlimited                                                                                                                                                                                                                                                      | json   | 33                 | once/day                                       | European Central Bank                                                                                                                                                |
| https://fixer.io                                              | 1000                                                                                                                                                                                                                                                           | json   | 168                | hourly (free) or faster, depending on the plan | *"Exchange rate data delivered by the Fixer API is collected from over 15 reliable data sources, every minute. Sources include banks and financial data providers."* |
| https://openexchangerates.org                                 | 1000                                                                                                                                                                                                                                                           | json   | 171                | hourly (free) or faster, depending on the plan | *"collected from multiple reliable providers"*                                                                                                                       |
| https://exchangeratesapi.io                                   | 250                                                                                                                                                                                                                                                            | json   | >170               | once/day                                       | >15 exchange rate data sources                                                                                                                                       |
| https://coincalc.samruston.co.uk/currencies.json              | "secret" API of CoinCalc                                                                                                                                                                                                                                       | json   | 776                | ?                                              | ?                                                                                                                                                                    |



## Comparison of ECB and IMF data

ECB and IMF are both free and *official* data providers. However, they also both offer only limited currencies.

| Currency            | ISO 4217 | [ECB] | [IMF] |
|---------------------|----------|-------|-------|
| U.A.E. dirham       | AED      | -     | ✓     |
| Australian dollar   | AUD      | ✓     | ✓     |
| Bulgarian lev       | BGN      | ✓     | -     |
| Brunei dollar       | BND      | -     | ✓     |
| Brazilian real      | BRL      | ✓     | ✓     |
| Botswana pula       | BWP      | -     | ✓     |
| Canadian dollar     | CAD      | ✓     | ✓     |
| Swiss franc         | CHF      | ✓     | ✓     |
| Chilean peso        | CLP      | -     | ✓     |
| Chinese yuan        | CNY      | ✓     | ✓     |
| Colombian peso      | COP      | -     | ✓     |
| Czech koruna        | CZK      | ✓     | ✓     |
| Danish krone        | DKK      | ✓     | ✓     |
| Algerian dinar      | DZD      | -     | ✓     |
| Euro                | EUR      | ✓     | ✓     |
| U.K. pound          | GBP      | ✓     | ✓     |
| Hong Kong dollar    | HKD      | ✓     | -     |
| Croatian kuna       | HRK      | ✓     | -     |
| Hungarian forint    | HUF      | ✓     | -     |
| Indonesian rupiah   | IDR      | ✓     | -     |
| Israeli New Shekel  | ILS      | ✓     | ✓     |
| Indian rupee        | INR      | ✓     | ✓     |
| Icelandic króna     | ISK      | ✓     | -     |
| Japanese yen        | JPY      | ✓     | ✓     |
| Korean won          | KRW      | ✓     | ✓     |
| Kuwaiti dinar       | KWD      | -     | ✓     |
| Mauritian rupee     | MUR      | -     | ✓     |
| Mexican peso        | MXN      | ✓     | ✓     |
| Malaysian ringgit   | MYR      | ✓     | ✓     |
| Norwegian krone     | NOK      | ✓     | ✓     |
| New Zealand dollar  | NZD      | ✓     | ✓     |
| Omani rial          | OMR      | -     | ✓     |
| Peruvian sol        | PEN      | -     | ✓     |
| Philippine peso     | PHP      | ✓     | ✓     |
| Polish zloty        | PLN      | ✓     | ✓     |
| Qatari riyal        | QAR      | -     | ✓     |
| Romanian leu        | RON      | ✓     | -     |
| Russian ruble       | RUB      | ✓     | ✓     |
| Saudi Arabian riyal | SAR      | -     | ✓     |
| Swedish krona       | SEK      | ✓     | ✓     |
| Singapore dollar    | SGD      | ✓     | ✓     |
| Thai baht           | THB      | ✓     | ✓     |
| Turkish lira        | TRY      | ✓     | -     |
| Trinidadian dollar  | TTD      | -     | ✓     |
| U.S. dollar         | USD      | ✓     | ✓     |
| Uruguayan peso      | UYU      | -     | ✓     |
| South African rand  | ZAR      | ✓     | ✓     |

