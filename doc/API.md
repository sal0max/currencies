# APIs

There are a number of (free) currency rate APIs available. They all are designed basically the same,
so it should be possible to switch APIs without having to change the data model too much.

This app currently uses *exchangeratesapi.io*. It's free and reliable.

service                                  | free api requests/month | currencies | updates                                        | data source
---------------------------------------- | ----------------------- | ---------- | ---------------------------------------------- | --------------------------------
exchangeratesapi.io                      | unlimited               |  32        | once/day                                       | European Central Bank
frankfurter.app                          | unlimited               |  32        | once/day                                       | European Central Bank
fixer.io                                 | 1000                    | 168        | hourly (free) or faster, depending on the plan | *"Exchange rate data delivered by the Fixer API is collected from over 15 reliable data sources, every minute. Sources include banks and financial data providers."*
openexchangerates.org                    | 1000                    | 171        | hourly (free) or faster, depending on the plan | *"collected from multiple reliable providers"*
coincalc.samruston.co.uk/currencies.json | secret API of CoinCalc  | 776        | ?                                              | ?
