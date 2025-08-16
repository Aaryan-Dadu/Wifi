# MMMUT WiFi Auto Login

An Android application that automatically logs into MMMUT WiFi networks when connected.

## Features

- **Automatic Detection**: Automatically detects when connected to MMMUT WiFi networks
- **Secure Storage**: Uses EncryptedSharedPreferences to securely store credentials
- **Background Operation**: Works silently in the background using WorkManager
- **Modern UI**: Clean, modern Material Design interface
- **Status Tracking**: Shows last login time and result
- **Toggle Control**: Easy enable/disable auto-login functionality

## Requirements

- Android 8.0 (API level 26) or higher
- WiFi permissions
- Network access permissions
- Location permissions (required for WiFi SSID access on Android 8.0+)

## Installation

1. Open Android Studio
2. Import the project
3. Build and run on your Android device

## OR

- **Just Download the MMMUT Wifi Auto Login.apk file**

## How It Works

1. **Setup**: Enter your roll number and password in the app
2. **Enable**: Turn on the auto-login toggle
3. **Automatic**: When you connect to any WiFi network starting with "MMMUT", the app automatically sends a login request to the portal
4. **Status**: View login status and last login time in the app

## Network Configuration

The app is configured to work with the MMMUT login portal at:
- **URL**: http://172.16.1.3:8090/httpclient.html
- **SSID Pattern**: Networks starting with "MMMUT"

## Security Features

- Credentials are encrypted using Android's EncryptedSharedPreferences
- Login requests are made over the local network only
- No credentials are logged or transmitted outside the login process
- Credentials are excluded from device backups

## Permissions Used

- **ACCESS_WIFI_STATE**: To detect WiFi connection status
- **ACCESS_NETWORK_STATE**: To monitor network connectivity
- **INTERNET**: To send login requests
- **ACCESS_FINE_LOCATION**: Required for WiFi SSID access on Android 8.0+
- **RECEIVE_BOOT_COMPLETED**: To start monitoring after device restart
- **WAKE_LOCK**: To perform login operations when device is sleeping

## Technical Details

- **Architecture**: MVVM with LiveData and ViewBinding
- **Background Processing**: WorkManager for reliable background execution
- **Network**: OkHttp for HTTP requests
- **Security**: EncryptedSharedPreferences for credential storage
- **UI**: Material Design 3 components

## Troubleshooting

### Login Not Working
- Ensure you're connected to a MMMUT WiFi network
- Check that your credentials are correct
- Verify auto-login is enabled
- Check the status section for error messages

### App Not Responding to WiFi Changes
- Ensure all required permissions are granted
- Check that the SSID starts with "MMMUT"
- Try toggling auto-login off and on again

### Permission Issues
- Grant all requested permissions in Android settings
- Location permission is required for WiFi SSID access

## Developer Notes

### Modifying Login Parameters
The login parameters can be modified in `LoginService.java`:
```java
RequestBody formBody = new FormBody.Builder()
    .add("username", rollNumber)
    .add("password", password)
    .add("mode", "191")
    .add("a", System.currentTimeMillis() + "")
    .add("producttype", "0")
    .build();
```

### Changing Target SSID
Modify the SSID prefix in `WiFiConnectionReceiver.java`:
```java
private static final String MMMUT_SSID_PREFIX = "MMMUT";
```

### Updating Login URL
Change the login URL in `LoginService.java`:
```java
private static final String LOGIN_URL = "http://172.16.1.3:8090/httpclient.html";
```

## License

This project is developed for educational purposes for MMMUT students and staff.

## Contributing

Feel free to submit issues, fork the repository, and create pull requests for any improvements.

## Disclaimer

This app is not officially affiliated with MMMUT. Use responsibly and in accordance with your institution's IT policies.
