package com.jiagu.ags4.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.scene.mine.MineModel
import com.jiagu.ags4.vm.AccountModel
import com.jiagu.ags4.vm.LoginTypeEnum
import com.jiagu.api.helper.CountryHelper
import com.jiagu.jgcompose.shadow.ShadowFrame
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.textfield.NormalTextField

@Composable
fun LoginTypeCombo(
    modifier: Modifier = Modifier,
    vm: AccountModel,
    width: Dp = 120.dp,
    height: Dp = 48.dp,
    imeAction: ImeAction = ImeAction.Done
) {
    val typeList = stringArrayResource(id = R.array.login_type).toList()
    val countryCodeIconList = mutableListOf<Int>()
    val countryCodeList = CountryHelper.COUNTRY_CODE.toList()
    countryCodeList.forEachIndexed { index, _ ->
        countryCodeIconList.add(CountryHelper.COUNTRY_FLAG[index])
    }
    //账号有效校验
    val accountValid = (vm.checkAccount() && vm.text.value.isNotBlank())
    vm.accountValid = accountValid
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShadowFrame {
            ComboBox(
                items = typeList,
                selectedValue = typeList[vm.type.intValue],
                onIndexChange = {
                    vm.type.intValue = it
                },
                width = width,
                fontStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(color = MaterialTheme.colorScheme.onPrimary),
            )
        }
        if (vm.type.intValue == 0) {
            ShadowFrame {
                ComboBox(
                    items = countryCodeList,
                    leftIcons = countryCodeIconList,
                    selectedValue = countryCodeList[vm.code.intValue],
                    selectedIndex = vm.code.intValue,
                    onIndexChange = {
                        vm.code.intValue = it
                    },
                    width = width,
                    fontStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(color = MaterialTheme.colorScheme.onPrimary),
                )
            }
        }
        ShadowFrame {
            NormalTextField(
                text = vm.text.value,
                onValueChange = { vm.text.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.small
                    ),
                hint = typeList[vm.type.intValue],
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = if (!accountValid) MaterialTheme.colorScheme.error else Color.Black
                ),
                borderColor = if (!accountValid && vm.text.value.isNotBlank()) MaterialTheme.colorScheme.error else Color.Transparent,
                hintTextStyle = MaterialTheme.typography.bodySmall,
                keyboardOptions = KeyboardOptions(imeAction = imeAction)
            )
        }
    }
}

@Composable
fun OneTypeCombo(
    modifier: Modifier = Modifier,
    vm: MineModel,
    width: Dp = 120.dp,
    type: LoginTypeEnum,
    imeAction: ImeAction = ImeAction.Done
) {
    var value by remember {
        mutableStateOf("")
    }
    val typeList = stringArrayResource(id = R.array.login_type)
    val countryCodeIconList = mutableListOf<Int>()
    val countryCodeList = CountryHelper.COUNTRY_CODE.toList()
    countryCodeList.forEachIndexed { index, _ ->
        countryCodeIconList.add(CountryHelper.COUNTRY_FLAG[index])
    }
    vm.text.value = value
    //账号有效校验
    val accountValid = (vm.checkAccount() && value.isNotBlank())
    vm.accountValid = accountValid
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (type == LoginTypeEnum.PHONE_NUMBER) {
            vm.type.intValue = 0
            ShadowFrame {
                Box(
                    modifier = Modifier
                        .width(width)
                        .fillMaxHeight()
                        .background(
                            color = MaterialTheme.colorScheme.onPrimary,
                            shape = MaterialTheme.shapes.small
                        ), contentAlignment = Alignment.Center
                ) {
                    AutoScrollingText(
                        text = stringResource(id = R.string.phone_number),
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            ShadowFrame {
                ComboBox(
                    items = countryCodeList,
                    leftIcons = countryCodeIconList,
                    selectedValue = countryCodeList[vm.code.intValue],
                    selectedIndex = vm.code.intValue,
                    onIndexChange = {
                        vm.code.intValue = it
                    },
                    width = width,
                    fontStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(color = MaterialTheme.colorScheme.onPrimary),
                )
            }
        } else if (type == LoginTypeEnum.EMAIL) {
            vm.type.intValue = 1
            ShadowFrame {
                Box(
                    modifier = Modifier
                        .width(width)
                        .fillMaxHeight()
                        .background(
                            color = MaterialTheme.colorScheme.onPrimary,
                            shape = MaterialTheme.shapes.small
                        ), contentAlignment = Alignment.Center
                ) {
                    AutoScrollingText(
                        text = stringResource(id = R.string.email),
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        ShadowFrame {
            NormalTextField(
                text = value,
                onValueChange = {
                    value = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.small
                    ),
                hint = typeList[vm.type.intValue],
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = if (!accountValid) MaterialTheme.colorScheme.error else Color.Black
                ),
                borderColor = if (!accountValid && value.isNotBlank()) MaterialTheme.colorScheme.error else Color.Transparent,
                hintTextStyle = MaterialTheme.typography.bodySmall,
                keyboardOptions = KeyboardOptions(imeAction = imeAction)
            )
        }
    }
}