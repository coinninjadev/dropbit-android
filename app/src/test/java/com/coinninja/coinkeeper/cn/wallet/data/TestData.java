package com.coinninja.coinkeeper.wallet.data;

// Scenario:
//
//    * 1st is a receive
//      -- using external address index 0
//
//    * 2nd is a send
//      -- using interal address index 0
//    * 3rd is a send
//      -- using interal address index 1
public class TestData {

    // Top 1
    public static final String[] EXTERNAL_ADDRESSES = {
            "3PxEH5t91Cio4B7LCZCEWQEGGxaqGW5HkX",
            "38YFML6k9NA4sjyUe9QnWYzwWwwUmbQuv9",
            "32jKmYp5BDokTKzv4XGdiw99FisMYojvGT",
            "3PDpvg3ay5mBRSvsQwqjL88XK9t9uBSegj",
            "3DfPhcLqammheyjiZD7nNzG1fDrpX1eymr",
            "3QQpvzaH4FGWiE8FXvbJi4hzKoUtbiFAdq",
            "3MDPQ81CG1TFTZ7AMp6Q4y65bDvnVFDXe4",
            "3PY4yb2zAUAwuz48VyDWBxcy82HWqx6D2J",
            "35B2YXWAxJRGvh7jKMKu4FWNZ9sUnbYnvn",
            "328pE7AJwtUDmuoA5TdhbEtXz9NvHAjTrK",
            "33aaqLv8gtSTWHCQmACvviDCmwXiUY4w3f",
            "3BePkTkwpF1YXnHYzWwevXhKUJcU5CGBDV",
            "3FtkdUpJyw3qF9MPTnxt1mpvZcFuQkBnQX",
            "3R1TWsW8HoZsmccKENzRnfevdeya58RcbB",
            "3QgPpW1XkwmcT1rwigUGeDVfJDvwsHctBq",
            "3JqJ5838SXxeJWe7mfDxn3XBVrqAmfMccH",
            "327DS4uPcCn6PCqHvWcxaRygvrN4TJEZbD",
            "3FZsa38Kv6Z6NQYjD6FXb9D55K4eyfvBSU",
            "3BKNXLhnY5nfjsBiVwCfwhTsfXJVQZem8a",
            "3KuKS7kEMCZDz5MbXeN15Ewiop54TkiDi9",
            "3ETC3oACsQFRQ3TvkJtmAurj1B5kD8hGqy",
            "3BsgF1tpnpF56gJqXqQAUQUK3hH1XvZ54U",
            "3NwgjBMsFJKWuZQ6SjRmU19T51exQynTyk",
            "32cL5jGYSSsdGbkZALcU3b99rG2NZSbPaG",
            "3DD3DvgSHqAUiAPDYm8vNJGy7kvMtkssyS",
            "3QydovfV5pPrKpfZ7Eiqj4MparPrqLQ9oo",
            "3Qn4nX3xj891GZvunVfJ4u7HjsiPNoKjFw",
            "3LPJkFTwph4FQw9con59CxLofmEN4jtVug",
            "3PqheBs1ErYrah5EFRQSRhfpEDx1NDigFz",
            "3JopxXwwcS2NSJwFuAQaD7xomjiFv4A6pu",
            "3QG312gAvpb7SoGRoA2qgUgWPPSrXr9H1g",
            "3N4BFaADpTPcLtxxMyQbpF8zUP8tFLhSp2",
            "37xJ3tHdQnjYDuu68k6bQrpVEPhL8FCe5o",
            "32ihjikt5AHuBqN2h9D4jstSSXSgVgtk19",
            "3FLxxdXpmsL8nvcf5M6kJJtz6DJBHBZi2r",
            "39VkSY5TpKYGx8PmtaWMR5jXVHBMZN8jWT",
            "32mvFmZ3Pdirm8PNWzw6YBuLJsYjb41pGA",
            "3HeUmcN21NaSG14kMLZzTk9te2WNFGA4v1",
            "3KfdpBNuzCZxNrNrqC4myUfG24xfW2eRY1",
            "3PH7LLtD6qWiiCx6Yu6vKiqTzh1AiUX4Fk",
            "3E5kz4gyVn6Ue2oifSLZyAR6kRrB793krF",
            "31xPDBRWiBW1Vv7unkn1gVKmv5qFuB255C",
            "3DUGn9rm5k13MmWJHV8t3ARiUVjBgNWMct",
            "3GgHyPWjA87SJz2zrezLGU1c8CWkpfPwLN",
            "3Q4kTCiP6srJqtLJ4mo986mm754zpYk2qG",
            "3KnJC741BFDdn3S6Wet6vcymNP77i3UnEk",
            "3Gjr1UaShQRcJjZVpzVrxYnt3Uaq298XR7",
            "3Qh3JGsDsk4EC24NaviYJkRFoUbpG55x8b",
            "3P8TxV4ZeS8aoCv72FYxtrWFuigqKdoMf4",
            "3L3etH4DrQwiteKHA7gYon2nAxmgoducbp",
            "3AFBUTudDWPybU6qhsCV6QGFyo7LoggCZx",
            "3HiZh1YGqTUzNJrJjpqUGpxrc6wALamzuf",
            "35qFcpsCiSirWXjqaE7RKJknnGBtbXwfR4",
            "3PqfGq8UEJ9suQTTLkYDPEYJJWTcaa8EJ9",
            "3Jc5SbU77g52Qgp7aYZtCYvBKYV4dqGPJM",
            "3GyZFoKhG67zMfozPpC7w6gSLvjsSsfDCj",
            "3GEu1QmKcdJBV2ymDJzogSBi6NCtYUgyXY",
            "3FUtVYh2RGi1FFF8RP42Vsec5wbtNiunQZ",
            "3QSwkUhYjfaypCjHKBejGs6v7MakfjRmjC",
            "39sf5AiksZsW4pTrv4KQzbbqh7qm4qNjzP",
            "38Y9UGzA9PWNUuekVoSgTAbwK8CT2TvS1c",
            "3HtJyZcJeqbJSxWbzBCfZcwURDZfFHfBmy",
            "3PiAdYYUyLLQh659AuC5ryNKmnxQywWxtQ",
            "37ET6bpHt1ZwTRAczAkd9qXxJ86vRT1bB1",
            "39xHEBa5T2k7e36PxrF1eef396DQo4Yf9V",
            "31yqmseVoZELc3cVmNyDpHN2ji9ucAevgq",
            "3Q8o7PuiU5NayiWcuBqSYBni6dAkx4EeiD",
            "3GEp4HJQnmtfd9jdVE2bWWLTh7ndDryvzV",
            "39S9c1ScCpK24FShW4bF1uck5nJM3d18jk",
            "3GvCVxBHQvLyiuJkdHi4JvMJebcB3FLFCC",
            "3936DNPzmAZFC3Ux5xfZChbkQq3M99S3Qa",
            "334BQ6LdCtiYt361NiheehrpMfBzxrUjN2",
            "35yCZrLujUJBM5xWmffN83wqaWiqgDop58",
            "3J3JDnkKTYdzTjAUqHkY7dQbqphMXbYLJi",
            "3QeBEk1mnHeAw8fo5Xcm9jZBNYh6yTiBDX",
            "37uc2Ss2Jiw7wSFcPBMp1xALBbMjAMDtTP",
            "36jq4ddoFBrUKfpQANwGQgrACNqscoWYUy",
            "3KcfCMUwhmxhcGt6TwvyhLkqNuDegraq8q",
            "3CfREgY5ChW4mqjbUutqV8a7jgTCXuPhK1",
            "3NBLzuHX7akSsZhg3GSeViXLwSyoXmGcPD",
            "3Kyg6pPqbj3PAPyKfMNptGoobCLiiLW7hh",
            "35nvuJzjB33e5gyU9LYSNmaByc4x4766Nj",
            "3A1QJN8DRBvt49iaie6g1KRwVuLvFpj3st",
            "3JaAuXMqnBoVZ1PMtj27xjDkYEcnnCGFRi",
            "39kQAc21HWSfFKUCLSiNgk8hrqe5SMZkee",
            "3HWoBzP3bhUH5y6Eug7UQ3cVNUguA6DdeP",
            "37XsmQeLaGLxtFK2RpptcE8w7cexxhYYi9",
            "3FQA75k9f4yt3MmZ1gdALA7NWfau3bYqqb",
            "3DHjh1Nqvnb1oYgNdsCDd672FabN9djAnY",
            "3JZkzJj5FXDZaci57F8pn5dukRgoEh9k2a",
            "32JdgsKkJpRaXmuNBueuRVprnraPdhv3Vr",
            "3Dys9BwAHBK6gEKGtZSKmBZDxxgff9ZgnK",
            "39447uAog8XDRhu8CgyR3pXpwRDgVdudcj",
            "3CKAyogNoMLPu7m4VWuWX5WH46NNAMyUvx",
            "3H6PptHMZcYZrN3WhxD15VTR6aAbUE6ap6",
            "3J4nT7T7ANdk6E11KaFe3Rd6tVContZBki",
            "3HuFbqXp49zuSDR3c7j3shDvFNHPSHjuKD",
            "3FkhgoiD6zeuvjRc41c9agu3CZTD881R2B",
            "32jkLeo3z29kmTdPZMt1yaRn7vUBd46wLL",
            "39W1Hpx4AeYhDGGmzMN73ExDABze3fB6rt"
    };

    public static final String[] INTERNAL_ADDRESSES = {
            "35VdVLAowYfb3jqvhAFGXJpjEuo9fP3c3e",
            "31oXo9w3nvRtiS3qaUw9d475dEqrvZJhdd",
            "3E55eveyWS4EuVsqpwk9fue4rqj8t9xeH7",
            "3FKH8q7Hj87eSA2oXcs18jvLaZgWRUsSMH",
            "3EKGaBcAA14wvzi5mz5BFmYkHxu9DfPSjG",
            "328NGLPyMktiPJCyhamYNipaL9DrNkq9PQ",
            "35EwueNF8wNmhKaKykAMMBcvJUowGNdkek",
            "3KmSKE9xxHWMUmZnsJdk2y3g7NbYsaQxoP",
            "34CExh1giHinNpnq1qrpkSJeoNJq1Af6dR",
            "33F66dH61x1CQnc15wEd9SNAr8uFGpHqZ3",
            "3GMS4ttmhpHztpkfn3G2571NCXgsZuciDW",
            "31mRKYX9qZLWzkCE8Z4dcFgs1Sx7JVES7Q",
            "37hZxhCTenVmMEGDpZDeGhPxDUTmPbyr6w",
            "35uZjuXHg8BxpLB4AwEaoymsjmN73Mrntf",
            "3L7iUZFgedQg1gL9vw4RvSFK8FC3XZ3sSZ",
            "35eGkGnC9gqYa8YxJgDG38YLF5v3DFSNCD",
            "3DiwcTU1ySdi5QBGxUd7reU7pBTN3kVxwZ",
            "33wftcoBsizTtyHajwi4E2HV6XfcRMMD33",
            "3FKpJtDyzzypcQFv95ddMtsapUnsVJnpRV",
            "3J7xXhm1EZqAU6xAq6vUdGK21MuSKh7oLT",
            "3Kv3Ru9zkVLNdfpnUdZTYUaoVGFHvKdUku",
            "38EbwH26KfVg2D3faDdZ49iJC2UHsKH2Su",
            "32fWUbhm3uEcCFsYhT3W5dTS4yAHwzdx2t",
            "3GWoKzRtQ9qRTRYjHYyUq9XkPfZUKB48Mv",
            "3B4dbavSiybmTuNTYYCeiBaLB1Yb3qGkBX",
            "32dLm5tAbAg4ZWRXHANiG8TjW9AWrS5mPA",
            "36NM6DyScWkijPi4Fm2AtboRCpCf3X2kH9",
            "3LbPogmBLp9H5TzXBGwQVgTQMxu6d4Kq4b",
            "3NPUJJxLjHQoMw9abYQAkM4JJTVqVeWDvL",
            "336RupeMWnfGQxLbvWeh1RFkjRyzK6iLar",
            "3NwaXxdcqGVKSMArQCt55uqZqoNNUAiQQN",
            "3LCAA4bsQnL83ZwG69YUzEvtT3zTSqaJBi",
            "32R9vAea8pMKM6dTQCABYYfKuiBjhxLQ39",
            "3EshC6ECuDXVVr8gDZtfEdLpNCaRjy6Y1w",
            "3GFyU9aAvEAKL7XE8dywRfYwknXxpKaXiT",
            "3J5PoZgg1adQjc6soNv2hHm5WTXZgdwms5",
            "3LR2Wz6Yx82y6WCMFtyuQwgxakGbZoyYjx",
            "3DKgSYuqr5FcdcPH2pP2anWr8bkhv6Xs7h",
            "38bhfuuLgCaUiya83kxehahGRd6kzYvg6w",
            "3ETZNLhokAUrU1LaXwpJUfisbpxRM3WxwK",
            "359PjRGVBwR8cZ55ywZwE2nzNq9wiQKP2F",
            "348BxQhtw4XT7HHc2aj2Qc2PBcRHZU1eVo",
            "3MA1GUpsF5sBAMSsN3ctoNVbgFkHM36Ghs",
            "3FZ1TPqbJan188RK6J2jjwKrZbyo16FQ3s",
            "3MPVy2qUXHxm1o1MNnHD51jhNg7bVokUps",
            "34bBE9aodw9gKqtXLw2TrzedqxdjnHTh6Z",
            "39tutGfeR4En55fY52hjSztgAA811nq5jd",
            "36tuSRqVf517yJBgLyy6Q9sfTUJCvRiBZx",
            "3Bxr5orLa5esUh7uMTHsh1HzMBmnzznezK",
            "38nW2kALjSdjw7idmFKsj5ZQgC6CNDo4gR",
            "3Bpo4FRJ4CUbkJiNLb2o7ZgxSSBdSDwbmv",
            "3MSvikSvTG3vjSZrzfuuhz4jzDARPtg3no",
            "3CxEHb6F9v8jiW2uDRasZE6P9D9b64Xd2W",
            "38BSD7DT9mRdWUBCYDnX6muyLosoEQuR5X",
            "38W94E4msJqEPmeJZuBuGwNQQYmj2rp2Zg",
            "3LqHDyit6gFsfKrT2upecGKy2p4hoeuiZm",
            "3Pw5d5NP8psn1oksjQwXSBxfqkvEkCqEgV",
            "3Lg8PGdtcqVfe1r1kngkvX7dJXpD43Vrw5",
            "37mM8iV4q2h1vqA7n28v3B9TUeMiMhwjbU",
            "37s4e9iXUK9LoxGGAXXEKyrFkhzvDj5nFF",
            "3HtXrF9npAqYvHn4ELhTa9ZezcAkgdPdkB",
            "3BxuDvcHdM4RHg4HaxYwpUGPT16QG96GMx",
            "3Bb9dyoRNFNVZMALnq8ZKqdRkdqtnypzFf",
            "3KxqfSb76nsLhkWURfBRccjjLfy94WinxS",
            "3AKk7sZvQJa6D3HRHGZ1YJZ881xXywjudP",
            "3CKphUbYqWoHY7Vxma8S1i4S2RTVgQbxUo",
            "3K6XDfv73h2XUtMdrQ1wAXsQzzD6Tyk4gy",
            "3QkWGwCxFWvHQwrFkrDG5BG4uMWzD2E9kK",
            "3AUET3ftoP1RzNHF9W49sn8Z8KGVRcpyuc",
            "3KQpJTFspDe9TuPbAAmzcxCf7mxSfFjLGo",
            "39CQCFBrV45EUFVeaVMnggTGQQd9C8nK5r",
            "3Bp9kroXfznXXrQ1amjw9KbQw9GvUDcnTS",
            "33oShzs5T8nWK8uKof36y6DrUysARU5izr",
            "3Nch8LnrGghuWUzeM1trH7Hf6RfLqoe7Wg",
            "3KcAcYd4N4rFjfreoNSeeXZ19xjLL9zE3Y",
            "36MMYVtukuKUv1nfkfh6xYC7ePy6q6FWRB",
            "3EAYgjY1Q6EPS3MaabphCzxBeFj91Feh3c",
            "3HibmPE2ht7bJ32Je68QS6JdpZAzqcFXHD",
            "3BEhCDZd2x7D8dNEvCygauDf2zKf9dWc1p",
            "3GzrCxfhXN2JtuXs9buGUWEEtnhmHcLZyx",
            "3EV8q8K7SWAE1vyrcy3CT1YEaAct6ed74N",
            "3GhrkWBm8WPWdJJbifSeB85k7DgttWR7P1",
            "34ae2ZiDcxK5okE4rvixP8MX1XBqTSaLsb",
            "3MTqWYGNHyCqsPRjmGLotNS2Q2vFZ235hM",
            "3AiuRSYxvMi1nSkVSjALAwVdZXdkYMZAzG",
            "3Qu19U3Kphfq45kdX2MsrNy9izH5uHS25y",
            "3ABtFr9L6ChgXaPQueCx2drm9bGDAx3wnS",
            "3NVhSDZrkkHNKMTX9JYzeEq1HKsLzoQKg5",
            "3Kh48HmYvqtLY3zc3akJ8AjcxyXRv8bqYF",
            "35hir6s5jGubVNX1JyTdc3Vbtrv3wjqDhU",
            "3Hh2kpBCYDFYkf6KuUSBVEpY3NitVXA6gQ",
            "3LCYFgW4Hq2rXeEbB8FUsF8wMq4VfhmBaN",
            "39uLpQMzmM64VdjLVF9XQsxRfbdiTitLzx",
            "3LsqvXBvuQuAGnoTrWyLqwxmqcQ575uhoN",
            "35yPzUGybz2rLe2wBzxwhzrcrDrDAQCcPE",
            "35K8Vh1QDSHJZ93zzthLYAXY13gQrPcSdX",
            "34hAjUPqm4ZNp5AEDbCpwP3WEXm9bfAYnE",
            "3ChSS5PiG45uaYfh4m36kZ7GVEdee7xhMy",
            "33QDGcneNe7FP7UckKos1HAfuNLTn5czjA",
            "38QsNY9gVLBdvpiQZ73XLDWJHvCrwc6JHK"
    };

    public static String EXTERNAL_ADDRESS_RESPONSE_BLOCK_ONE = "" +
            "[" +
            "   {" +
            "       \"address\": \"3PxEH5t91Cio4B7LCZCEWQEGGxaqGW5HkX\"," +
            "       \"txid\": \"1d1ef96bc636952cc01d7d613df41caf7815e6766670bafa5d096bacf843fd24\"," +
            "       \"time\": 1521560498," +
            "       \"vin\": 507070," +
            "       \"vout\": 0" +
            "   }," +
            "   {" +
            "       \"address\": \"3PxEH5t91Cio4B7LCZCEWQEGGxaqGW5HkX\"," +
            "       \"txid\": \"9cbe0f300ffa3581f729c5f2a35686a14643ac4d20cb2f7018a173736ac40c57\"," +
            "       \"time\": 1520526378," +
            "       \"vin\": 0," +
            "       \"vout\": 507070" +
            "   }," +
            "   {" +
            "       \"address\": \"38YFML6k9NA4sjyUe9QnWYzwWwwUmbQuv9\"," +
            "       \"txid\": \"9cbe0f300ffa3581f729c5f2a35686a14643ac4d20cb2f7018a173736ac40c57\"," +
            "       \"time\": 1520526378," +
            "       \"vin\": 0," +
            "       \"vout\": 507070" +
            "   }," +
            "   {" +
            "       \"address\": \"38YFML6k9NA4sjyUe9QnWYzwWwwUmbQuv9\"," +
            "       \"txid\": \"9cbe0f300ffa3581f729c5f2a35686a14643ac4d20cb2f7018a173736ac40c57\"," +
            "       \"time\": 1520526378," +
            "       \"vin\": 0," +
            "       \"vout\": 507070" +
            "   }" +
            "]";

    public static String INTERNAL_ADDRESS_RESPONSE_BLOCK_ONE = "" +
            "[" +
            "   {" +
            "       \"address\": \"35VdVLAowYfb3jqvhAFGXJpjEuo9fP3c3e\"," +
            "       \"txid\": \"1d1ef96bc636952cc01d7d613df41caf7815e6766670bafa5d096bacf843fd24\"," +
            "       \"time\": 1521574898," +
            "       \"vin\": 0," +
            "       \"vout\": 395165" +
            "   }," +
            "]";
    public static String EXTERNAL_ADDRESS_RESPONSE_BLOCK_TWO = "[]";

    public static String TRANSACTIONS_ONE = "" +
            "{" +
            "   \"txid\": \"1d1ef96bc636952cc01d7d613df41caf7815e6766670bafa5d096bacf843fd24\"," +
            "   \"hash\": \"1d1ef96bc636952cc01d7d613df41caf7815e6766670bafa5d096bacf843fd24\"," +
            "   \"blockhash\": \"0000000000000000003a5354f8a8f5b79acca05f380c861912f4900c766cbef1\"," +
            "   \"height\": 1770," +
            "   \"version\": 1," +
            "   \"size\": 373," +
            "   \"vsize\": 373," +
            "   \"weight\": 1492," +
            "   \"time\": 1521560498," +
            "   \"blocktime\": 1521560498," +
            "   \"locktime\": 0," +
            "   \"coinbase\": false," +
            "   \"confirmations\": 4770," +
            "   \"blockheight\": 0," +
            "   \"receivedtime\": 0," +
            "   \"vout\": [" +
            "        {" +
            "            \"value\": 110000," +
            "            \"n\": 0," +
            "            \"scriptPubKey\": {" +
            "                    \"asm\": \"OP_DUP OP_HASH160 4b8db04f92cc966052f877eae1d8f2bfa8d915c9 OP_EQUALVERIFY OP_CHECKSIG\"," +
            "                    \"hex\": \"76a9144b8db04f92cc966052f877eae1d8f2bfa8d915c988ac\"," +
            "                    \"reqSigs\": 1," +
            "                    \"type\": \"pubkeyhash\"," +
            "                    \"addresses\": [" +
            "                       \"17tVR6KbCacSn2P5Fp5Bn9NkxpfeA6Bhnp\"" +
            "                    ]" +
            "            }" +
            "        }," +
            "        {" +
            "            \"value\": 395165," +
            "            \"n\": 1," +
            "            \"scriptPubKey\": {" +
            "                    \"asm\": \"OP_HASH160 29b933341348681d32ac7c4a623d2e5019e7f1bc OP_EQUAL\"," +
            "                    \"hex\": \"a91429b933341348681d32ac7c4a623d2e5019e7f1bc87\"," +
            "                    \"reqSigs\": 1," +
            "                    \"type\": \"scripthash\"," +
            "                    \"addresses\": [" +
            "                       \"35VdVLAowYfb3jqvhAFGXJpjEuo9fP3c3e\"" +
            "                    ]" +
            "            }" +
            "        }" +
            "    ]," +
            "   \"vin\": [" +
            "         {" +
            "               \"txid\": \"69151603ebe4192d50c1aaaca4e0ab0ea335184e261376c2eda64c35ce9fd1b5\"," +
            "               \"vout\": 1," +
            "               \"scriptSig\": {" +
            "                  \"asm\": \"00142f0908d7a15b75bfacb22426b5c1d78f545a683f\"," +
            "                  \"hex\": \"1600142f0908d7a15b75bfacb22426b5c1d78f545a683f\"" +
            "         }," +
            "         \"txinwitness\": [" +
            "               \"304402204dcaba494328bd472f4bf61761e43c9ca204ea81ce9c5c57d669e4ed4721499f022007a6024b0f5e202a7f38bb90edbecaa788e276239a12aa42d958818d52db3f9f01\"," +
            "               \"036ebf6ab96773a9fa7997688e1712ddc9722ef9274220ba406cb050ac5f1a1306\"" +
            "         ]," +
            "         \"sequence\": 4294967295," +
            "         \"previousoutput\": {" +
            "               \"value\": 999934902," +
            "               \"n\": 1," +
            "               \"scriptPubKey\": {" +
            "                     \"asm\": \"OP_HASH160 4f7728b2a54dc9a2b44e47341e7e029bb99c7d72 OP_EQUAL\"," +
            "                     \"hex\": \"a9144f7728b2a54dc9a2b44e47341e7e029bb99c7d7287\"," +
            "                     \"reqSigs\": 1," +
            "                     \"type\": \"scripthash\"," +
            "                     \"addresses\": [" +
            "                            \"38wC41V2tNZrr2uiwUthn41b2M8SLGMVRt\"" +
            "                    ]" +
            "                }" +
            "            }" +
            "        }" +
            "    ]," +
            "    \"blocks\" : [ " +
            "           \"0000000000000000003a5354f8a8f5b79acca05f380c861912f4900c766cbef1\"" +
            "     ]" +
            "}";

    public static String TRANSACTIONS_TWO = "" +
            "{" +
            "   \"txid\": \"9cbe0f300ffa3581f729c5f2a35686a14643ac4d20cb2f7018a173736ac40c57\"," +
            "   \"hash\": \"9cbe0f300ffa3581f729c5f2a35686a14643ac4d20cb2f7018a173736ac40c57\"," +
            "   \"blockhash\": \"00000000000000000043e7c7461a0f95af578786ab8660bc9b2242ebbdeb364a\"," +
            "   \"height\": 1245," +
            "   \"version\": 1," +
            "   \"size\": 223," +
            "   \"vsize\": 223," +
            "   \"weight\": 892," +
            "   \"time\": 1520526378," +
            "   \"blocktime\": 1520526378," +
            "   \"locktime\": 0," +
            "   \"coinbase\": false," +
            "   \"confirmations\": 6559," +
            "   \"blockheight\": 0," +
            "   \"receivedtime\": 0," +
            "    \"vout\": [" +
            "        {" +
            "            \"value\": 507070," +
            "            \"n\": 0," +
            "            \"scriptPubKey\": {" +
            "                    \"asm\": \"OP_HASH160 f43351c45f3900f75c4da664b3571ff17c319fa5 OP_EQUAL\"," +
            "                    \"hex\": \"a914f43351c45f3900f75c4da664b3571ff17c319fa587\"," +
            "                    \"reqSigs\": 1," +
            "                    \"type\": \"scripthash\"," +
            "                    \"addresses\": [" +
            "                       \"3PxEH5t91Cio4B7LCZCEWQEGGxaqGW5HkX\"" +
            "                    ]" +
            "            }" +
            "        }," +
            "        {" +
            "            \"value\": 52626400," +
            "            \"n\": 1," +
            "            \"scriptPubKey\": {" +
            "                    \"asm\": \"OP_DUP OP_HASH160 6d82ef76ccb8923dd8c30442fc6cdd3aec1b8575 OP_EQUALVERIFY OP_CHECKSIG\"," +
            "                    \"hex\": \"76a9146d82ef76ccb8923dd8c30442fc6cdd3aec1b857588ac\"," +
            "                    \"reqSigs\": 1," +
            "                    \"type\": \"pubkeyhash\"," +
            "                    \"addresses\": [" +
            "                       \"1Az3WTjyGtr41CBZN2z4uifXc6KDNhdrqE\"" +
            "                    ]" +
            "            }" +
            "        }" +
            "    ]," +
            "   \"vin\": [" +
            "         {" +
            "               \"txid\": \"89e7888b6e743d1cd5e6375153e6b2f7950d9675c72a6c93eb1c11599913b5e1\"," +
            "               \"vout\": 1," +
            "               \"scriptSig\": {" +
            "                  \"asm\": \"30440220572c0bc7fd6f2163a7dfefae3dea9af6a9e153fb2ce3291ab5e8e25e52a2dcca0220274062655cb051070e68f2b55169fba43e15215d495f95a6a124e93f3d8a1a6901 039ffab0462a321285886f981255649fd7ab001e4900f75ce822c3fba39315cf16\"," +
            "                  \"hex\": \"4730440220572c0bc7fd6f2163a7dfefae3dea9af6a9e153fb2ce3291ab5e8e25e52a2dcca0220274062655cb051070e68f2b55169fba43e15215d495f95a6a124e93f3d8a1a690121039ffab0462a321285886f981255649fd7ab001e4900f75ce822c3fba39315cf16\"" +
            "         }," +
            "         \"txinwitness\": [" +
            "         ]," +
            "         \"sequence\": 4294967295," +
            "         \"previousoutput\": {" +
            "               \"value\": 0," +
            "               \"n\": 0," +
            "               \"scriptPubKey\": {" +
            "                     \"asm\": \"\"," +
            "                     \"hex\": \"\"," +
            "                     \"reqSigs\": 0," +
            "                     \"type\": \"\"," +
            "                     \"addresses\": [" +
            "                    ]" +
            "                }" +
            "            }" +
            "        }" +
            "    ]," +
            "    \"blocks\" : [ " +
            "           \"00000000000000000043e7c7461a0f95af578786ab8660bc9b2242ebbdeb364a\"" +
            "     ]" +
            "}";

    public static final String TRANSACTION_ONE_STATS = "{\n" +
            "       \"txid\": \"1d1ef96bc636952cc01d7d613df41caf7815e6766670bafa5d096bacf843fd24\",\n" +
            "       \"coinbase\": false,\n" +
            "       \"confirmations\": 5039,\n" +
            "       \"fees_rate\": 5107,\n" +
            "       \"fees\": 1905,\n" +
            "       \"miner\": \"GBMiners\",\n" +
            "       \"vin_value\": 507070,\n" +
            "       \"vout_value\": 505165\n" +
            "   }";

    public static final String TRANSACTION_TWO_STATS = "{\n" +
            "       \"txid\": \"9cbe0f300ffa3581f729c5f2a35686a14643ac4d20cb2f7018a173736ac40c57\",\n" +
            "       \"coinbase\": false,\n" +
            "       \"confirmations\": 6675,\n" +
            "       \"fees_rate\": 10448,\n" +
            "       \"fees\": 2330,\n" +
            "       \"miner\": \"GBMiners\",\n" +
            "       \"vin_value\": 53135800,\n" +
            "       \"vout_value\": 53133470\n" +
            "   }";
    public static final String TRANSACTION_ONE_CONFIRMATION = "{\n" +
            "   \"txid\": \"1d1ef96bc636952cc01d7d613df41caf7815e6766670bafa5d096bacf843fd24\"," +
            "   \"confirmations\": 5039" +
            "}";
    public static final String TRANSACTION_TWO_CONFIRMATION = "{\n" +
            "   \"txid\": \"9cbe0f300ffa3581f729c5f2a35686a14643ac4d20cb2f7018a173736ac40c57\"," +
            "   \"confirmations\": 6675" +
            "}";
}
