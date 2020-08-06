import * as functions from 'firebase-functions';
import axios from 'axios';

// Inorder to test functions locally execute with "npm run build && firebase emulators:start"

export const getListOfModules = functions.https.onRequest((request, response) => {
    response.status(200).send('{data: ["Clock","Weather","Stocks","News Feed"]}');
});


// http://localhost:5001/displayhubcompanion/us-central1/getNewsUpdates?request=ynet usage example to test
export const getNewsUpdates = functions.https.onRequest(async (request, response) => {
    const dataRequest = request.query.request;
    switch (dataRequest) {
        case "ynet":
            response.status(200).send(await parseYnetNewsFeed());
            break;
        case "jpost":
            response.status(200).send(await parseJPost());
            break;
        default:
            response.status(403).send("Unknown parameter");
    }
});

// -> response
// >  {
// >    dayofweek: 1,
// >    dayofweekName: 'Monday',
// >    day: 8,
// >    month: 6,
// >    monthName: 'June',
// >    year: 2020,
// >    hours: 1,
// >    minutes: 19,
// >    seconds: 31,
// >    millis: 377,
// >    fulldate: 'Mon, 08 Jun 2020 01:19:31 +0300',
// >    timezone: 'Asia/Jerusalem',
// >    status: 'ok'
//     >  }
//https://github.com/davidayalas/current-time api site
export const getTime = functions.https.onRequest(async (request, response) => {
    const xml_result = await axios.get("https://script.google.com/macros/s/AKfycbyd5AcbAnWi2Yn0xhFRbyzS4qMq1VucMVgVvhul5XqS9HkAyJY/exec?tz=Asia/Jerusalem");
    if (xml_result.status === 200 && xml_result.statusText === 'OK') {
        console.log(xml_result.data)
        response.status(200).send(xml_result.data);
    } else {
        response.status(200).send("");
    }
});

// http://localhost:5001/displayhubcompanion/us-central1/getWeather?country=Israel&city=Haifa usage example to test
export const getWeather = functions.https.onRequest(async (request, response) => {
    try {
        const dataRequest = request.query.city;
        const state = request.query.country;
        const openWeatherToken = "e3fd5b18d7a012955d5b1ebb6e0566b9";
        const openWeatherApiBaseAdress = "http://api.openweathermap.org/data/2.5/weather?q=";
        const openWeatherRequest = openWeatherApiBaseAdress + dataRequest + "," + state + "&appid=" + openWeatherToken
        const xml_result = await axios.get(openWeatherRequest);
        if (xml_result.status === 200 && xml_result.statusText === 'OK') {
            console.log(xml_result.data);
            const return_data = {
                state: state,
                city: dataRequest,
                temp: (xml_result.data.main.temp - 273.15).toFixed(2), // Kelvin to celcius
                description: xml_result.data.weather[0].description,
                humidity: xml_result.data.main.humidity, // %
                wind: xml_result.data.wind.speed  // m/sec
            };
            response.status(200).send(return_data);
        } else {
            console.log("getWeather: Something went wrong! (Invalid result status)");
            response.status(200).send("getWeather: Something went wrong! (Invalid result status)")
        }
    } catch (e) {
        console.log(e);
    }

});

// '_' is a separator
//call with GET request(for now) http://localhost:5001/displayhubcompanion/us-central1/getStocks?request=ibm_intc
export const getStocks = functions.https.onRequest(async (request, response) => {
    try {
        // @ts-ignore
        const dataRequest: string = request.query.request.toString();
        const stocksResponse: StocksResponse = new StocksResponse();
        const symbols: string[] = dataRequest.split("_");
        for (const symbol of symbols) {
            try {
                const stockUrl = 'https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=' + symbol + '&interval=5min&apikey=BQ4I9KHXS2Y28AW8';
                const result = await axios.get(stockUrl);
                // @ts-ignore
                let stock: string = Object.values(result.data['Time Series (5min)'])[0]['4. close'];
                const index_of_dot = stock.indexOf(".");
                stock = stock.substring(0, index_of_dot + 2);
                stocksResponse.stocks.push(new Stock(symbol.toLowerCase(), stock));
            } catch (e) {
                console.log(e);
            }
        }
        response.status(200).send(JSON.stringify(stocksResponse));
    } catch (e) {
        console.log(e);
    }
});

class StocksResponse {
    public stocks: Stock[] = new Array<Stock>();
}

class Stock {
    constructor(public name: string, public price: string) {
    }
}


async function parseYnetNewsFeed(): Promise<string> {
    const xml_link = "http://www.ynet.co.il/Integration/StoryRss3254.xml"
    const xml_result = await axios.get(xml_link);
    if (xml_result.status === 200 && xml_result.statusText === 'OK') {
        const news_lines_arr: RegExpMatchArray | null = xml_result.data.match(/<title>[^<]*<\/title>/g);
        if (news_lines_arr) {
            return news_lines_arr.reduce((acc, n) =>
                acc + n.replace("<title>", "").replace("<\/title>", "\n").replace("&quot;", '"')
            );
        } else {
            console.log("parseYnetNewsFeed: Something went wrong! (No news)");
            return "";
        }
    } else {
        console.log("parseYnetNewsFeed: Something went wrong! (Invalid result status)");
        return "";
    }
}

async function parseJPost(): Promise<string> {
    const jpost = "https://www.jpost.com/rss/rssfeedsfrontpage.aspx"
    const jpost_result = await axios.get(jpost);
    if (jpost_result.status === 200 && jpost_result.statusText === 'OK') {
        const news_lines_arr: RegExpMatchArray | null = jpost_result.data.match(/<title>[^<]*<\/title>/g);
        if (news_lines_arr) {
            news_lines_arr.splice(0, 1);
            news_lines_arr.splice(10, news_lines_arr.length - 10);
            return news_lines_arr.reduce((acc, n) =>
                acc + n.replace(new RegExp("<title>", "g"), "").replace(new RegExp("<\/title>", "g"), "\n").replace(new RegExp("&quot;", "g"), '"').replace(new RegExp("&#39;", "g"), "'")
                , "");
        } else {
            console.log("parseJPost: Something went wrong! (No news)");
            return "";
        }
    } else {
        console.log("parseJPost: Something went wrong! (Invalid result status)");
        return "";
    }
}
