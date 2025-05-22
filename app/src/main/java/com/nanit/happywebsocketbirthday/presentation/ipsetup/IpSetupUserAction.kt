package com.nanit.happywebsocketbirthday.presentation.ipsetup

sealed interface IpSetupUserAction {
    data class IpPortChanged(val newIpPort: String) : IpSetupUserAction // for IP/Port changes
    data object ConnectClicked : IpSetupUserAction  // for Connect button click
    data object DisconnectClicked : IpSetupUserAction //for Disconnect button click
    data object SendMessageClicked : IpSetupUserAction//for SendMessage button click
    data object BabyInfoNavigationHandled : IpSetupUserAction
}