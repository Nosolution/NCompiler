package lex.entity;

import lombok.Data;

/**
 * Rule part of lex file, containing regex pattern and its action
 *
 * @author Nosolution
 * @version 1.0
 * @since 2019/12/6
 */
@Data
public class Rule {
    private String pattern;
    private String action;
}
