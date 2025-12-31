package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.textfield.LeftIconTextField
import com.jiagu.jgcompose.textfield.NormalTextField
import com.jiagu.jgcompose.textfield.NumberInputField
import com.jiagu.jgcompose.textfield.PasswordTextField
import com.jiagu.jgcompose.theme.ComposeTheme

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun AllTextFieldPreviews() {
    ComposeTheme {
        FlowRow(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // NormalTextField preview
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var normalText by remember { mutableStateOf("") }
                var emailText by remember { mutableStateOf("") }
                
                NormalTextField(
                    modifier = Modifier.height(40.dp),
                    text = normalText,
                    onValueChange = { normalText = it },
                    backgroundColor = Color.White,
                    hint = "Please enter text",
                    isLengthLimit = true,
                    maxInputLength = 20
                )
                
                NormalTextField(
                    modifier = Modifier.height(40.dp),
                    text = emailText,
                    onValueChange = { emailText = it },
                    backgroundColor = Color.White,
                    hint = "Please enter email address",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }
            
            // PasswordTextField preview
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var passwordText by remember { mutableStateOf("") }
                
                PasswordTextField(
                    modifier = Modifier.height(40.dp),
                    text = passwordText,
                    onValueChange = { passwordText = it },
                    backgroundColor = Color.White,
                    hint = "Please enter password"
                )
            }
            
            // LeftIconTextField preview
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var iconText by remember { mutableStateOf("") }
                
                LeftIconTextField(
                    modifier = Modifier.height(40.dp),
                    text = iconText,
                    onValueChange = { iconText = it },
                    backgroundColor = Color.White,
                    hint = "Input field with icon",
                    leftIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "email icon"
                        )
                    }
                )
            }
            
            // NumberInputField preview
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                var numberText by remember { mutableStateOf("0") }
                
                NumberInputField(
                    modifier = Modifier.height(180.dp),
                    number = numberText,
                    onConform = { numberText = it }
                )
            }
        }
    }
}