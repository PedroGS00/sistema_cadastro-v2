package com.sistema.cadastro.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CepDataDTO {

    @JsonProperty("cep")
    private String cep;

    @JsonProperty("logradouro")
    private String logradouro;

    @JsonProperty("complemento")
    private String complemento;

    @JsonProperty("bairro")
    private String bairro;

    @JsonProperty("localidade")
    private String localidade;

    @JsonProperty("uf")
    private String uf;

    @JsonProperty("ibge")
    private String ibge;

    @JsonProperty("gia")
    private String gia;

    @JsonProperty("ddd")
    private String ddd;

    @JsonProperty("siafi")
    private String siafi;

    @JsonProperty("erro")
    private Boolean erro;

    public boolean isValid() {
        return erro == null || !erro;
    }

    public String getFullAddress() {
        StringBuilder fullAddress = new StringBuilder();
        
        if (logradouro != null && !logradouro.isEmpty()) {
            fullAddress.append(logradouro);
        }
        
        if (bairro != null && !bairro.isEmpty()) {
            if (fullAddress.length() > 0) {
                fullAddress.append(" - ");
            }
            fullAddress.append(bairro);
        }
        
        if (localidade != null && !localidade.isEmpty()) {
            if (fullAddress.length() > 0) {
                fullAddress.append(", ");
            }
            fullAddress.append(localidade);
        }
        
        if (uf != null && !uf.isEmpty()) {
            if (fullAddress.length() > 0) {
                fullAddress.append(" - ");
            }
            fullAddress.append(uf);
        }
        
        return fullAddress.toString();
    }

    public String getCep() { return cep; }
    public String getLogradouro() { return logradouro; }
    public String getComplemento() { return complemento; }
    public String getBairro() { return bairro; }
    public String getLocalidade() { return localidade; }
    public String getUf() { return uf; }
    public String getIbge() { return ibge; }
    public String getGia() { return gia; }
    public String getDdd() { return ddd; }
    public String getSiafi() { return siafi; }
    public Boolean getErro() { return erro; }

    public void setCep(String cep) { this.cep = cep; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }
    public void setComplemento(String complemento) { this.complemento = complemento; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public void setLocalidade(String localidade) { this.localidade = localidade; }
    public void setUf(String uf) { this.uf = uf; }
    public void setIbge(String ibge) { this.ibge = ibge; }
    public void setGia(String gia) { this.gia = gia; }
    public void setDdd(String ddd) { this.ddd = ddd; }
    public void setSiafi(String siafi) { this.siafi = siafi; }
    public void setErro(Boolean erro) { this.erro = erro; }
}
