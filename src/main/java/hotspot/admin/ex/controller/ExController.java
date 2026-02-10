package hotspot.admin.ex.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.admin.ex.controller.port.FindExService;
import hotspot.admin.ex.controller.port.SaveExService;
import hotspot.admin.ex.controller.port.UpdateExService;
import hotspot.admin.ex.controller.request.CreateExRequest;
import hotspot.admin.ex.controller.request.UpdateExRequest;
import hotspot.admin.ex.controller.response.ExResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ex")
public class ExController {

  private final FindExService findExService;
  private final SaveExService saveExService;
  private final UpdateExService updateExService;

  @GetMapping("/{exId}")
  public ResponseEntity<ExResponse> find(@PathVariable Long exId) {
    return ResponseEntity.ok(findExService.findEx(exId));
  }

  @PostMapping
  public ResponseEntity<Void> save(@RequestBody CreateExRequest request) {
    saveExService.save(request);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{exId}")
  public ResponseEntity<Void> update(
      @PathVariable Long exId, @RequestBody UpdateExRequest request) {
    updateExService.update(exId, request);
    return ResponseEntity.noContent().build();
  }
}
