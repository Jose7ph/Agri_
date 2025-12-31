
### Set the signingConfigs on Build.gradle.kts

```
    signingConfigs {
        getByName("debug") {
            storeFile = file("******")
            storePassword = "******"
            keyAlias = "******"
            keyPassword = "******"
        }
    }
```

### Set the mapbox api key on string_mapbox.xml
```xml
<resources>
    <string name="mapbox_access_token" translatable="false">YOUR_API_KEY</string><!--hang-->
</resources>
```

### Ser the google map api key on AndroidManifest.xml
```xml
<meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="YOUR_API_KEY" />
```