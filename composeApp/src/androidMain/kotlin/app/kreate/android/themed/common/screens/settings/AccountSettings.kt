package app.kreate.android.themed.common.screens.settings

import android.webkit.CookieManager
import android.webkit.WebStorage
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.kreate.android.Preferences
import app.kreate.android.R
import app.kreate.android.coil3.ImageFactory
import app.kreate.android.service.innertube.InnertubeProvider
import app.kreate.android.themed.common.component.settings.RestartPlayerService
import app.kreate.android.themed.common.component.settings.SettingComponents
import app.kreate.android.themed.common.component.settings.SettingEntrySearch
import app.kreate.android.themed.common.component.settings.animatedEntry
import app.kreate.android.themed.common.component.settings.entry
import app.kreate.android.themed.common.component.settings.header
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.extensions.discord.DiscordLoginAndGetToken
import it.fast4x.rimusic.extensions.youtubelogin.YouTubeLogin
import it.fast4x.rimusic.thumbnailShape
import it.fast4x.rimusic.ui.components.CustomModalBottomSheet
import it.fast4x.rimusic.ui.styling.Dimensions

//Placeholder Composable for Spotify Login 
@Composable
fun SpotifyLogin( onDone: () -> Unit ) {
    // Placeholder content for the Spotify Login UI (likely a WebView)
    Column(modifier = Modifier.padding(16.dp)) {
        SettingComponents.Text(
            title = "Spotify Login Placeholder",
            subtitle = "Implement your Spotify OAuth logic here.",
            onClick = { /* Actual login logic */ onDone() }
        )
    }
}

@ExperimentalMaterial3Api
@Composable
fun AccountSettings( paddingValues: PaddingValues ) {
    val context = LocalContext.current
    val scrollState = rememberLazyListState()

    val search = remember {
        SettingEntrySearch( scrollState, R.string.tab_accounts, R.drawable.person )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.background( colorPalette().background0 )
                           .padding( paddingValues )
                           .fillMaxHeight()
                           .fillMaxWidth(
                               if ( NavigationBarPosition.Right.isCurrent() )
                                   Dimensions.contentWidthRightBar
                               else
                                   1f
                           )
    ) {
        search.ToolBarButton()

        LazyColumn(
            state = scrollState,
            contentPadding = PaddingValues(bottom = Dimensions.bottomSpacer)
        ) {
            // This is a brand name that doesn't need translation
            header( { "youtube" } )
            entry( search, "youtube" ) {
                SettingComponents.BooleanEntry(
                    Preferences.YOUTUBE_LOGIN,
                    stringResource( R.string.setting_entry_youtube_login )
                ) {
                    if( it ) return@BooleanEntry

                    Preferences.YOUTUBE_VISITOR_DATA.reset()
                    Preferences.YOUTUBE_SYNC_ID.reset()
                    Preferences.YOUTUBE_COOKIES.reset()
                    Preferences.YOUTUBE_ACCOUNT_NAME.reset()
                    Preferences.YOUTUBE_ACCOUNT_EMAIL.reset()
                    Preferences.YOUTUBE_SELF_CHANNEL_HANDLE.reset()
                }
            }
            animatedEntry(
                key = "ytLoginChildren",
                visible = Preferences.YOUTUBE_LOGIN.value,
                modifier = Modifier.padding( start = 25.dp )
            ) {
                var loginYouTube by remember { mutableStateOf(false) }
                val isLoggedIn by remember {derivedStateOf {
                    "SAPISID" in InnertubeProvider.COOKIE_MAP
                }}

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if ( isLoggedIn && Preferences.YOUTUBE_ACCOUNT_AVATAR.value.isNotEmpty() )
                            ImageFactory.AsyncImage(
                                thumbnailUrl = Preferences.YOUTUBE_ACCOUNT_AVATAR.value,
                                contentDescription = "YouTube account's avatar",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.height( 50.dp )
                                                   .clip( thumbnailShape() )
                            )

                        val (title, subtitle) = remember( isLoggedIn, Preferences.YOUTUBE_ACCOUNT_NAME, Preferences.YOUTUBE_SELF_CHANNEL_HANDLE ) {
                            if ( isLoggedIn )
                                "Disconnect" to "%s %s".format( Preferences.YOUTUBE_ACCOUNT_NAME.value, Preferences.YOUTUBE_SELF_CHANNEL_HANDLE.value )
                            else
                                "Connect" to ""
                        }
                        if( search appearsIn title )
                            SettingComponents.Text(
                                title = title,
                                subtitle = subtitle,
                                onClick = {
                                    if (isLoggedIn) {

                                        Preferences.YOUTUBE_VISITOR_DATA.reset()
                                        Preferences.YOUTUBE_SYNC_ID.reset()
                                        Preferences.YOUTUBE_COOKIES.reset()
                                        Preferences.YOUTUBE_ACCOUNT_NAME.reset()
                                        Preferences.YOUTUBE_ACCOUNT_EMAIL.reset()
                                        Preferences.YOUTUBE_SELF_CHANNEL_HANDLE.reset()
                                        Preferences.YOUTUBE_ACCOUNT_AVATAR.reset()
                                        loginYouTube = false
                                        //Delete cookies after logout
                                        val cookieManager = CookieManager.getInstance()
                                        cookieManager.removeAllCookies(null)
                                        cookieManager.flush()
                                        WebStorage.getInstance().deleteAllData()

                                        RestartPlayerService.requestRestart()
                                    } else
                                        loginYouTube = true
                                },
                            ) {
                                Icon(
                                    painter = painterResource( R.drawable.ytmusic ),
                                    contentDescription = title,
                                    tint = colorPalette().text,
                                    modifier = Modifier.size( 24.dp )
                                )
                            }
                    }

                    val syncPlaylistsTitle = stringResource(
                        R.string.setting_entry_sync_quotes,
                        stringResource( R.string.playlists )
                    )
                    if( search appearsIn syncPlaylistsTitle )
                        SettingComponents.BooleanEntry(
                            preference = Preferences.YOUTUBE_PLAYLISTS_SYNC,
                            title = syncPlaylistsTitle
                        )

                    val syncArtistsTitle = stringResource(
                        R.string.setting_entry_sync_quotes,
                        stringResource( R.string.artists )
                    )
                    if( search appearsIn syncArtistsTitle )
                        SettingComponents.BooleanEntry(
                            preference = Preferences.YOUTUBE_ARTISTS_SYNC,
                            title = syncArtistsTitle
                        )

                    val syncAlbumsTitle = stringResource(
                        R.string.setting_entry_sync_quotes,
                        stringResource( R.string.albums )
                    )
                    if( search appearsIn syncAlbumsTitle )
                        SettingComponents.BooleanEntry(
                            preference = Preferences.YOUTUBE_ALBUMS_SYNC,
                            title = syncAlbumsTitle
                        )
                }

                CustomModalBottomSheet(
                    showSheet = loginYouTube,
                    onDismissRequest = { loginYouTube = false },
                    containerColor = colorPalette().background0,
                    contentColor = colorPalette().background0,
                    modifier = Modifier.fillMaxWidth(),
                    sheetState = rememberModalBottomSheetState( true ),
                    dragHandle = {
                        Surface(
                            color = colorPalette().background0,
                            shape = thumbnailShape()
                        ) {}
                    },
                    shape = Preferences.THUMBNAIL_BORDER_RADIUS.value.shape
                ) {
                    YouTubeLogin {
                        loginYouTube = false

                        if( !isLoggedIn ) {
                            Preferences.YOUTUBE_VISITOR_DATA.reset()
                            Preferences.YOUTUBE_SYNC_ID.reset()
                            Preferences.YOUTUBE_COOKIES.reset()
                            Preferences.YOUTUBE_ACCOUNT_NAME.reset()
                            Preferences.YOUTUBE_ACCOUNT_EMAIL.reset()
                            Preferences.YOUTUBE_SELF_CHANNEL_HANDLE.reset()
                            Preferences.YOUTUBE_ACCOUNT_AVATAR.reset()
                        }
                    }
                }
            }

            // *** START OF SPOTIFY LOGIN SECTION ***
            header( { "spotify" } )
            entry( search, "spotify_login" ) {
                SettingComponents.BooleanEntry(
                    // Preference to enable Spotify services
                    Preferences.SPOTIFY_LOGIN,
                    stringResource(R.string.setting_entry_spotify_login) // Hardcoded string for simplicity, replace with stringResource(R.string.setting_entry_spotify_login)
                ) {
                    if ( it ) return@BooleanEntry

                    // Logic to clear Spotify credentials on disable
                    Preferences.SPOTIFY_ACCESS_TOKEN.reset()
                }
            }
            animatedEntry(
                key = "spotifyLoginChildren",
                visible = Preferences.SPOTIFY_LOGIN.value,
                modifier = Modifier.padding( start = 25.dp )
            ) {
                var loginSpotify by remember { mutableStateOf(false) }
                val isLoggedIn = Preferences.SPOTIFY_ACCESS_TOKEN.value.isNotBlank()

                // Resolve string resources OUTSIDE the remember block
                val disconnectTitle = stringResource(R.string.spotify_canvas_disconnect) 
                val connectedSubtitle = stringResource(R.string.spotify_canvas_connected) 
                val connectTitle = stringResource(R.string.spotify_canvas_connect) 

                Column {
                    val (title, subtitle) = remember( isLoggedIn ) {
                        if ( isLoggedIn )
                             disconnectTitle to  connectedSubtitle
                        else
                             connectTitle to ""
                    }
// ...
,
                        ) {
                            
                            Image(
                                painter = painterResource( R.drawable.spotify_logo ),
                                contentDescription = title,
                                modifier = Modifier.size( 24.dp )
                            )
                        }

                    val spotifyCanvasTitle = stringResource(R.string.spotify_canvas_title)
                    if( search appearsIn spotifyCanvasTitle )
                        SettingComponents.BooleanEntry(
                            preference = Preferences.SPOTIFY_CANVAS_ENABLED,
                            title =  stringResource(R.string.setting_entry_spotify_canvas)
                        )
                }

                CustomModalBottomSheet(
                    showSheet = loginSpotify,
                    onDismissRequest = { loginSpotify = false },
                    containerColor = colorPalette().background0,
                    contentColor = colorPalette().background0,
                    modifier = Modifier.fillMaxWidth(),
                    sheetState = rememberModalBottomSheetState( true ),
                    dragHandle = {
                        Surface(
                            color = colorPalette().background0,
                            shape = thumbnailShape()
                        ) {}
                    },
                    shape = Preferences.THUMBNAIL_BORDER_RADIUS.value.shape
                ) {
                    // Call the placeholder or your actual SpotifyLogin composable
                    SpotifyLogin {
                        loginSpotify = false
                    }
                }
            }
            // *** END OF SPOTIFY LOGIN SECTION ***

            // This is a brand name that doesn't need translation
            header( { "discord" } )
            entry( search, R.string.discord_enable_rich_presence ) {
                SettingComponents.BooleanEntry(
                    Preferences.DISCORD_LOGIN,
                    R.string.discord_enable_rich_presence
                )
            }
            animatedEntry(
                key = "discordLoginChildren",
                visible = Preferences.DISCORD_LOGIN.value,
                modifier = Modifier.padding( start = 25.dp )
            ) {
                var loginDiscord by remember { mutableStateOf(false) }
                val (titleId, subtitle) = remember( Preferences.DISCORD_ACCESS_TOKEN.value ) {
                    if( Preferences.DISCORD_ACCESS_TOKEN.value.isBlank() )
                        R.string.discord_connect to ""
                    else
                        R.string.discord_disconnect to context.getString( R.string.discord_connected_to_discord_account )
                }
                if( search appearsIn titleId )
                    SettingComponents.Text(
                        title = stringResource( titleId ),
                        subtitle = subtitle,
                        onClick = {
                            loginDiscord = Preferences.DISCORD_ACCESS_TOKEN.value.isBlank()

                            if( !loginDiscord )
                                Preferences.DISCORD_ACCESS_TOKEN.reset()
                        }
                    ) {
                        Image(
                            painter = painterResource( R.drawable.discord_logo ),
                            contentDescription = null,
                            modifier = Modifier.size( 24.dp )
                        )
                    }

                CustomModalBottomSheet(
                    showSheet = loginDiscord,
                    onDismissRequest = { loginDiscord = false },
                    containerColor = colorPalette().background0,
                    contentColor = colorPalette().background0,
                    modifier = Modifier.fillMaxWidth(),
                    sheetState = rememberModalBottomSheetState( true ),
                    dragHandle = {
                        Surface(
                            color = colorPalette().background0,
                            shape = thumbnailShape()
                        ) {}
                    },
                    shape = Preferences.THUMBNAIL_BORDER_RADIUS.value.shape
                ) {
                    DiscordLoginAndGetToken { loginDiscord = false }
                }
            }
        }
    }
}
