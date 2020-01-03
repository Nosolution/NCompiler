package yacc.entity;


import lombok.Data;

/**
 * Description of class
 *
 * @author Nosolution
 * @version 1.0
 * @since 2019/12/21
 */
@Data
public class Token {
    int name;
    Object val;

    public Token(int name, Object val){
        this.name = name;
        this.val = val;
    }

    public Token(){
        this.name = 0;
        this.val = null;
    }

}
