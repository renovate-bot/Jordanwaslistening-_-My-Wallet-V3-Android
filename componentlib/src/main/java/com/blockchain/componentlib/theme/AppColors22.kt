package com.blockchain.componentlib.theme

import androidx.compose.ui.graphics.Color

private val Title = Color(0XFF121D33)
private val TitleNight = Color(0XFFFFFFFF)
private val Body = Color(0XFF50596B)
private val BodyNight = Color(0XFF989BA1)
private val Muted = Color(0XFF98A1B2)
private val MutedNight = Color(0XFF63676F)

private val Primary = Color(0XFF0C6CF2)
private val PrimaryNight = Color(0XFF65A5FF)
private val PrimaryMuted = Color(0XFF65A5FF)
private val PrimaryMutedNight = Color(0XFF1656B9)
private val PrimaryLight = Color(0XFFECF5FE)
private val PrimaryLightNight = Color(0XFF65A5FF) // missing
private val Light = Color(0XFFF0F2F7)
private val LightNight = Color(0XFF2C3038)
private val Dark = Color(0XFFB1B8C7)
private val DarkNight = Color(0XFF98A1B2)
private val Medium = Color(0XFFDFE3EB)
private val MediumNight = Color(0XFF3B3E46)
private val Success = Color(0XFF00B083)
private val SuccessNight = Color(0XFF69ECCA)
private val Negative = Color(0XFFF00699)
private val NegativeNight = Color(0XFFFF55B8)
private val Warning = Color(0XFFD46A00)
private val WarningNight = Color(0XFFFFA133)
private val WarningMuted = Color(0XFFFFA133)
private val WarningMutedNight = Color(0XFFFFA133) // missing
private val WarningLight = Color(0XFFFFECD6)
private val WarningLightNight = Color(0XFFFFECD6) // missing
private val Error = Color(0XFFCF1726)
private val ErrorNight = Color(0XFFFF3344)
private val ErrorLight = Color(0XFFFFD9D6)
private val ErrorLightNight = Color(0XFFFFD9D6) // missing

private val ExplorerLight = Color(0XFF5322E5)
private val ExplorerNight = Color(0XFF9080FF) // missing

private val Background = Color(0XFFF1F2F7)
private val BackgroundNight = Color(0XFF07080D)
private val BackgroundSecondary = Color(0XFFFFFFFF)
private val BackgroundSecondaryNight = Color(0XFF20242C)

private val Scrim = Color(0XA3121D33)
private val ScrimNight = Color(0XCC121D33)

val defLightColors = SemanticColors(
    title = Title, //
    body = Body, //
    overlay = Overlay600,
    muted = Muted, //
    dark = Dark, //
    medium = Medium, //
    light = Light, //
    background = Background, //
    backgroundSecondary = BackgroundSecondary, //
    primary = Primary, //
    primaryMuted = PrimaryMuted, //
    primaryLight = PrimaryLight, // missing dark
    success = Success, //
    successMuted = Green300,
    warning = Warning, //
    warningMuted = WarningMuted, // missing dark
    warningLight = WarningLight, // missing dark
    error = Error, //
    errorMuted = Red400,
    errorLight = ErrorLight,
    negative = Negative, //
    negativeMuted = Negative,
    semidark = Grey400,
    explorer = ExplorerLight,
    scrim = Scrim, //
    isLight = true
)

val defDarkColors = SemanticColors(
    title = TitleNight,
    body = BodyNight,
    overlay = Overlay600,
    muted = MutedNight,
    dark = DarkNight,
    medium = MediumNight,
    light = LightNight,
    background = BackgroundNight,
    backgroundSecondary = BackgroundSecondaryNight,
    primary = PrimaryNight,
    primaryMuted = PrimaryMutedNight,
    primaryLight = PrimaryLightNight,
    success = SuccessNight,
    successMuted = Green300,
    warning = WarningNight,
    warningMuted = WarningMutedNight,
    warningLight = WarningLightNight,
    error = ErrorNight,
    errorMuted = Red400,
    errorLight = ErrorLightNight,
    negative = NegativeNight,
    negativeMuted = NegativeNight,
    semidark = Grey800,
    explorer = ExplorerNight,
    scrim = ScrimNight,
    isLight = false,
)
