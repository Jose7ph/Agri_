package com.jiagu.ags4.scene.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.Validator
import com.jiagu.api.helper.CountryHelper
import com.jiagu.jgcompose.button.ComboImageListButton
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.shadow.ShadowFrame
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.textfield.NormalTextField

@Composable
fun TeamAddMemberNumber(finish: () -> Unit = {}) {
    val navController = LocalNavController.current
    val teamManagementModel = LocalTeamManagementModel.current
    MainContent(title = when (teamManagementModel.addMemberType) {
        AddMemberTypeEnum.PHONE -> stringResource(R.string.phone_number)
        AddMemberTypeEnum.EMAIL -> stringResource(R.string.email)
        else -> ""
    }, breakAction = {
        if (!navController.popBackStack()) finish()
    }) {
        var text by remember {
            mutableStateOf("")
        }
        var code by remember {
            mutableIntStateOf(0)
        }
        val countryCodeList = CountryHelper.COUNTRY_CODE.toList()
        val countryFlagList = CountryHelper.COUNTRY_FLAG.toList()

        var validator by remember { mutableStateOf(true) }
        if (text.isNotEmpty()) {
            if (teamManagementModel.addMemberType == AddMemberTypeEnum.PHONE) {
                validator = Validator.checkPhoneNumber(text)
            }
            if (teamManagementModel.addMemberType == AddMemberTypeEnum.EMAIL) {
                validator = Validator.checkEmail(text)
            }
        } else {
            validator = true
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.height(40.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (teamManagementModel.addMemberType) {
                    AddMemberTypeEnum.PHONE -> {
                        TitleShadowBox(text = stringResource(R.string.phone_number))
                        ShadowFrame {
                            ComboImageListButton(
                                modifier = Modifier.width(120.dp),
                                index = code,
                                items = countryCodeList,
                                value = countryCodeList[code],
                                images = countryFlagList,
                                onConfirm = {
                                    code = it
                                },
                                backgroundColor = Color.White,
                                textColor = Color.Black,
                                imageColorChange = false
                            )
                        }
                        ShadowFrame {
                            NormalTextField(
                                modifier = Modifier.fillMaxWidth(),
                                text = text,
                                onValueChange = {
                                    text = it
                                },
                                borderColor = if (validator) Color.Transparent else Color.Red,
                            )
                        }
                    }

                    AddMemberTypeEnum.EMAIL -> {
                        TitleShadowBox(text = stringResource(R.string.email))
                        ShadowFrame {
                            NormalTextField(
                                modifier = Modifier.fillMaxWidth(),
                                text = text,
                                onValueChange = {
                                    text = it
                                },
                                borderColor = if (validator) Color.Transparent else Color.Red,
                            )
                        }
                    }

                    else -> {}
                }
            }
            //confirm button
            ShadowFrame {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = MaterialTheme.shapes.small,
                    enabled = validator && text.isNotEmpty(),
                    onClick = {
                        var phones = text
                        if (teamManagementModel.addMemberType == AddMemberTypeEnum.PHONE) {
                            phones = "${countryCodeList[code]}-$text"
                        }
                        teamManagementModel.addTeamMembers(phones = phones) {
                            //团队信息页面数据更新
                            teamManagementModel.loadTeamInfos()
                            navController.popBackStack("team_add_member_menu", true)
                        }
                    },
                ) {
                    AutoScrollingText(
                        text = stringResource(id = R.string.confirm),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun TitleShadowBox(text: String) {
    ShadowFrame {
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.onPrimary,
                    shape = MaterialTheme.shapes.small
                ), contentAlignment = Alignment.Center
        ) {
            AutoScrollingText(
                text = text,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}