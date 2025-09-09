/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.utils;
import org.passay.*;

import java.util.Arrays;
/**
 *
 * @author Admin
 */
public class PasswordGeneratorUtil {
     public static String generatePassword() {
        PasswordGenerator gen = new PasswordGenerator();
        CharacterRule lowerCaseRule = new CharacterRule(EnglishCharacterData.LowerCase, 2);
        CharacterRule upperCaseRule = new CharacterRule(EnglishCharacterData.UpperCase, 2);
        CharacterRule digitRule = new CharacterRule(EnglishCharacterData.Digit, 2);
        CharacterRule specialCharRule = new CharacterRule(EnglishCharacterData.Special, 2);

        return gen.generatePassword(12, Arrays.asList(
                lowerCaseRule, upperCaseRule, digitRule, specialCharRule));
    }

   
}
