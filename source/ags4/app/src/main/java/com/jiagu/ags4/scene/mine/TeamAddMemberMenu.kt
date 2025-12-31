package com.jiagu.ags4.scene.mine

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.ext.noEffectClickable

@Composable
fun TeamAddMemberMenu(finish: () -> Unit = {}) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val teamManagementModel = LocalTeamManagementModel.current
    MainContent(title = stringResource(id = R.string.add_team_member), breakAction = {
        if (!navController.popBackStack()) finish()
    }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            //员工列表
            MemberMenuRow(
                image = R.drawable.default_mine_team_management,
                title = stringResource(R.string.staff_list),
                hint = stringResource(R.string.add_from_the_staff_list),
                onClick = {
                    teamManagementModel.addMemberType = AddMemberTypeEnum.STAFF_LIST
                    teamManagementModel.refreshTeamStaffList()
                    navController.navigate("team_add_member_staff_list")
                }
            )
            HorizontalDivider(thickness = 1.dp, modifier = Modifier.fillMaxWidth())
            //手机号
            MemberMenuRow(
                image = R.drawable.default_phone,
                title = stringResource(R.string.phone_number),
                hint = stringResource(R.string.add_via_mobile_number),
                onClick = {
                    teamManagementModel.addMemberType = AddMemberTypeEnum.PHONE
                    navController.navigate("team_add_member_number")
                }
            )
            HorizontalDivider(thickness = 1.dp, modifier = Modifier.fillMaxWidth())
            //邮箱
            MemberMenuRow(
                image = R.drawable.default_email,
                title = stringResource(R.string.email),
                hint = stringResource(R.string.add_via_mobile_number),
                onClick = {
                    teamManagementModel.addMemberType = AddMemberTypeEnum.EMAIL
                    navController.navigate("team_add_member_number")
                }
            )
            HorizontalDivider(thickness = 1.dp, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun MemberMenuRow(image: Int, title: String, hint: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .noEffectClickable {
                onClick()
            },
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = "staff list",
            modifier = Modifier.size(36.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = hint, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "arrow right",
            modifier = Modifier.size(36.dp)
        )
    }
}
