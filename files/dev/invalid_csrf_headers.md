### Received Header information.

#### Hitting - Login Token API

```
GeoIP=IN:TN:Chennai:13.09:80.28:v4; Path=/; secure; Domain=.wikimedia.org
commonswikiSession=dlgiv9hxxxx-dummy-xxxx971qevuhi; path=/; secure; HttpOnly
forceHTTPS=true; path=/; HttpOnly
WMF-Last-Access=24-Feb-2020;Path=/;HttpOnly;secure;Expires=Fri, 27 Mar 2020 12:00:00 GMT
```

#### Hitting - Client Login API (Login-Success)

```
forceHTTPS=deleted; expires=Thu, 01-Jan-1970 00:00:01 GMT; Max-Age=0; path=/; HttpOnly
centralauth_Session=22c3b2xxxx-dummy-xxxxa69f8ba6fc6; path=/; domain=commons.wikimedia.org; secure; HttpOnly
centralauth_User=Manimaran96; expires=Wed, 25-Mar-2020 18:52:58 GMT; Max-Age=2592000; path=/; domain=commons.wikimedia.org; secure; HttpOnly
commonswikiUserID=7560559; expires=Wed, 25-Mar-2020 18:52:58 GMT; Max-Age=2592000; path=/; secure; HttpOnly
commonswikiSession=u90rc4vijxxxx-dummy-xxxxcu22nu3a; path=/; secure; HttpOnly
loginnotify_prevlogins=2020-1xe7yuh-styc6kwxxxx-dummy-xxxx2tvfpd0p606xm; expires=Sat, 22-Aug-2020 18:52:58 GMT; Max-Age=15552000; path=/; secure; HttpOnly
commonswikiUserName=Manimaran96; expires=Wed, 25-Mar-2020 18:52:58 GMT; Max-Age=2592000; path=/; secure; HttpOnly
forceHTTPS=true; path=/; domain=commons.wikimedia.org; HttpOnly
```

#### Hitting - EditToken
```
centralauth_Session=e50a59xxxx-dummy-xxxx43f03024e; path=/; domain=commons.wikimedia.org; secure; HttpOnly
```

#### Successful audio upload
```
UseCDNCache=false; expires=Mon, 24-Feb-2020 19:57:42 GMT; Max-Age=10; path=/; secure; HttpOnly
cpPosIndex=1%401582574252%2xxxx-dummy-xxxx69372e8c576; expires=Mon, 24-Feb-2020 19:57:42 GMT; Max-Age=10; path=/; secure; HttpOnly
UseDC=master; expires=Mon, 24-Feb-2020 19:57:42 GMT; Max-Age=10; path=/; secure; HttpOnly
```

#### Invalid CSRF Token - Headers (Fail audio upload)

```
forceHTTPS=deleted; expires=Thu, 01-Jan-1970 00:00:01 GMT; Max-Age=0; path=/; HttpOnly
centralauth_User=Manimaran96; expires=Wed, 25-Mar-2020 20:09:21 GMT; Max-Age=2592000; path=/; domain=commons.wikimedia.org; secure; HttpOnly
centralauth_Session=04ab83404xxxx-dummy-xxxxfde006d2; path=/; domain=commons.wikimedia.org; secure; HttpOnly
commonswikiUserID=7560559; expires=Wed, 25-Mar-2020 20:09:21 GMT; Max-Age=2592000; path=/; secure; HttpOnly
commonswikiSession=rc8jjg0i5v8cxxxx-dummy-xxxxb6vv; path=/; secure; HttpOnly
commonswikiUserName=Manimaran96; expires=Wed, 25-Mar-2020 20:09:21 GMT; Max-Age=2592000; path=/; secure; HttpOnly
forceHTTPS=true; path=/; domain=commons.wikimedia.org; HttpOnly
```

#### Invalid CSRF Token - Response 

```
{
"error":{
    "code":"badtoken",
    "info":"Invalid CSRF token.",
    "*":"See https://commons.wikimedia.org/w/api.php for API usage. Subscribe to the mediawiki-api-announce mailing list at &lt;https://lists.wikimedia.org/mailman/listinfo/mediawiki-api-announce&gt; for notice of API deprecations and breaking changes."
    },
"servedby":"mw1277"
}
```
